package me.whizvox.otdl.file;

import lombok.extern.slf4j.Slf4j;
import me.whizvox.otdl.exception.*;
import me.whizvox.otdl.security.ComboAuthToken;
import me.whizvox.otdl.security.SecurityService;
import me.whizvox.otdl.storage.InputFile;
import me.whizvox.otdl.storage.StorageException;
import me.whizvox.otdl.storage.StorageService;
import me.whizvox.otdl.user.User;
import me.whizvox.otdl.util.EncryptedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.CipherOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class FileService {

  private static final int
      ID_LENGTH = 12;
  private static final char[] ID_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_".toCharArray();

  private final FileRepository repo;
  private final FileConfiguration config;
  private final SecurityService security;
  private final StorageService storage;
  private final Path tempDir;

  @Autowired
  public FileService(FileRepository repo, FileConfiguration config, SecurityService security, StorageService storage) {
    this.repo = repo;
    this.config = config;
    this.security = security;
    this.storage = storage;
    tempDir = Paths.get(config.getTempDirectoryLocation()).normalize().toAbsolutePath();

    try {
      Files.createDirectories(tempDir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String generateId() {
    SecureRandom rand = new SecureRandom();
    char[] ch = new char[ID_LENGTH];
    for (int i = 0; i < ch.length; i++) {
      ch[i] = ID_CHARS[rand.nextInt(ID_CHARS.length)];
    }
    return new String(ch);
  }

  private static MessageDigest createMD5() {
    try {
      return MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private static MessageDigest createSHA1() {
    try {
      return MessageDigest.getInstance("SHA1");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public Optional<FileInfo> getInfo(String id) {
    return repo.findById(id);
  }

  /**
   * Upload a file, then encrypt it.
   * @param file The file to be uploaded
   * @param lifespan How long the file will be kept in the file system in minutes
   * @param password The password used to encrypt the file
   * @param user The user to be attributed as the file's owner
   * @return An {@link FileInfo} instance
   * @throws IOException Writing to the output file is unsuccessful in some way
   */
  public FileInfo upload(MultipartFile file, int lifespan, char[] password, @Nullable User user) throws IOException {
    if (file == null) {
      throw new NoFileException();
    }
    if (lifespan < 1) {
      throw new InvalidLifespanException(lifespan);
    }
    Path encPath = tempDir.resolve(UUID.randomUUID().toString());
    String id = generateId();
    byte[] salt = security.generateSalt();
    MessageDigest md5 = createMD5();
    MessageDigest sha1 = createSHA1();
    try (InputStream in = file.getInputStream();
         OutputStream out = Files.newOutputStream(encPath);
         CipherOutputStream cout = new CipherOutputStream(out, security.createCipher(true, password, salt))) {
      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) != -1) {
        md5.update(buffer, 0, read);
        sha1.update(buffer, 0, read);
        cout.write(buffer, 0, read);
      }
    }
    storage.store(InputFile.local(encPath), id);
    FileInfo info = new FileInfo();
    try {
      info.setStoredSize(Files.size(encPath));
    } catch (IOException e) {
      info.setStoredSize(-1);
    }
    Files.delete(encPath);
    info.setId(id);
    info.setFileName(file.getOriginalFilename());
    info.setOriginalSize(file.getSize());
    info.setMd5(HexFormat.of().formatHex(md5.digest()));
    info.setSha1(HexFormat.of().formatHex(sha1.digest()));
    info.setAuthToken(security.getAuthTokenCodec().encodeToString(security.generateAuthToken(password, salt)));
    info.setUploaded(LocalDateTime.now());
    info.setExpires(info.getUploaded().plusMinutes(lifespan));
    info.setDownloaded(false);
    if (user != null) {
      info.setUser(user);
    }
    repo.save(info);
    return info;
  }

  /**
   * Serves the specified file as a resource for downloading.
   * @param id The ID of the file
   * @param password The password needed to decrypt the file
   * @param markForDeletion Whether to mark the file to be deleted
   * @return The resource to the file, or <code>null</code> if the file record could not be found
   * @throws FileMismatchException The record of the file is found but not the file itself
   * @throws WrongPasswordException An incorrect password is supplied
   */
  public Resource serve(String id, char[] password, boolean markForDeletion) {
    Optional<FileInfo> infoOp = getInfo(id);
    if (infoOp.isPresent()) {
      FileInfo info = infoOp.get();
      if (info.isDownloaded()) {
        return null;
      }
      if (!storage.exists(info.getId())) {
        throw new FileMismatchException("File does not exist");
      }
      ComboAuthToken token = security.getAuthTokenCodec().decode(info.getAuthToken());
      if (!token.authorize(security.generateSecret(password, token.getSalt()).getEncoded())) {
        throw new WrongPasswordException();
      }
      if (markForDeletion) {
        info.setDownloaded(true);
        info.setExpires(LocalDateTime.now().plusMinutes(config.getLifespanAfterAccess()));
        repo.save(info);
      }
      return new EncryptedResource(storage.openStream(info.getId()), security.createCipher(false, password, token.getSalt()), info.getFileName(), info.getOriginalSize());
    }
    return null;
  }

  /**
   * Deletes a specified file.
   * @param id The ID of the file to delete
   */
  public void delete(String id) {
    if (repo.existsById(id)) {
      repo.deleteById(id);
      storage.delete(id);
    }
  }

  public void delete(Iterable<String> ids){
    ids.forEach(this::delete);
    storage.delete(ids);
  }

  public int deleteExpiredFiles() {
    List<FileInfo> expiredFiles = repo.findAllExpired();
    if (!expiredFiles.isEmpty()) {
      for (FileInfo info : expiredFiles) {
        try {
          storage.delete(info.getId());
        } catch (StorageException e) {
          log.error("Could not delete physical file " + info.getId(), e);
        }
      }
      repo.deleteAll(expiredFiles);
      return expiredFiles.size();
    }
    return 0;
  }

  public void update(FileInfo file) {
    if (!repo.existsById(file.getId())) {
      throw new UnknownIdException();
    }
    repo.save(file);
  }

  public void clearUser(UUID userId) {
    int totalCount = repo.clearUser(userId);
    if (totalCount > 0) {
      log.info("Cleared user {} from {} file(s)", userId, totalCount);
    }
  }

  public Page<FileInfo> search(Specification<FileInfo> spec, Pageable pageable) {
    return repo.findAll(spec, pageable);
  }

  public Page<FileInfo> getFilesUploadedByUser(UUID userId, Pageable pageable) {
    return repo.findAllFilesUploadedBy(userId, pageable);
  }

  public Optional<FileInfo> getFileUploadedByUser(String fileId, UUID userId) {
    return repo.findFileUploadedBy(fileId, userId);
  }

  public long getCount() {
    return repo.count();
  }

  public long getStorageUsed() {
    if (getCount() == 0) {
      return 0;
    }
    // probably a good idea to cache this value?
    return repo.getStorageUsed();
  }

}
