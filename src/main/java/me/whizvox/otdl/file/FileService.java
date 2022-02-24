package me.whizvox.otdl.file;

import me.whizvox.otdl.exception.FileMismatchException;
import me.whizvox.otdl.exception.InvalidLifespanException;
import me.whizvox.otdl.exception.NoFileException;
import me.whizvox.otdl.exception.WrongPasswordException;
import me.whizvox.otdl.security.ComboAuthToken;
import me.whizvox.otdl.security.SecurityService;
import me.whizvox.otdl.util.EncryptedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.CipherOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

  private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

  private static final int
      ID_LENGTH = 12;
  private static final char[] ID_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_".toCharArray();

  private final FileRepository repo;
  private final FileConfiguration config;
  private final SecurityService security;
  private final Path rootDir;

  @Autowired
  public FileService(FileRepository repo, FileConfiguration config, SecurityService security) {
    this.repo = repo;
    this.config = config;
    this.security = security;

    rootDir = Path.of(config.getUploadedFilesDirectory()).normalize().toAbsolutePath();
    try {
      Files.createDirectories(rootDir);
    } catch (IOException e) {
      throw new RuntimeException("Could not create root directory for files service", e);
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
   * @return An {@link FileInfo} instance
   * @throws IOException Writing to the output file is unsuccessful in some way
   */
  public FileInfo upload(MultipartFile file, int lifespan, char[] password) throws IOException {
    if (file == null) {
      throw new NoFileException();
    }
    if (lifespan < 1 || lifespan > config.getMaxLifespanMember()) {
      throw new InvalidLifespanException(lifespan);
    }
    String id = generateId();
    Path path = rootDir.resolve(id);
    byte[] salt = security.generateSalt();
    MessageDigest md5 = createMD5();
    MessageDigest sha1 = createSHA1();
    try (InputStream in = file.getInputStream();
         OutputStream out = Files.newOutputStream(path);
         CipherOutputStream cout = new CipherOutputStream(out, security.createCipher(true, password, salt))) {
      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) != -1) {
        md5.update(buffer, 0, read);
        sha1.update(buffer, 0, read);
        cout.write(buffer, 0, read);
      }
    }
    FileInfo info = new FileInfo();
    info.setId(id);
    info.setOriginalSize(file.getSize());
    try {
      info.setStoredSize(Files.size(path));
    } catch (IOException e) {
      info.setStoredSize(-1);
    }
    info.setMd5(HexFormat.of().formatHex(md5.digest()));
    info.setSha1(HexFormat.of().formatHex(sha1.digest()));
    info.setAuthToken(security.getAuthTokenCodec().encodeToString(security.generateAuthToken(password, salt)));
    info.setUploaded(LocalDateTime.now());
    info.setExpires(info.getUploaded().plusMinutes(lifespan));
    info.setDownloaded(false);
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
      Path inputFilePath = rootDir.resolve(info.getId());
      if (!Files.exists(inputFilePath)) {
        throw new FileMismatchException("File does not exist");
      }
      ComboAuthToken token = security.getAuthTokenCodec().decode(info.getAuthToken());
      if (!token.authorize(security.generateSecret(password, token.getSalt()).getEncoded())) {
        throw new WrongPasswordException();
      }
      if (markForDeletion) {
        info.setDownloaded(true);
        info.setExpires(LocalDateTime.now().plusMinutes(config.getLifespanAfterAccess()));
      }
      return new EncryptedResource(inputFilePath, security.createCipher(false, password, token.getSalt()));
    }
    return null;
  }

  /**
   * Deletes a specified file.
   * @param id The ID of the file to delete
   * @throws IOException Could not delete the file from the file system
   */
  public void delete(String id) throws IOException {
    if (repo.existsById(id)) {
      Path filePath = rootDir.resolve(id);
      repo.deleteById(id);
      Files.deleteIfExists(filePath);
    }
  }

  public int deleteExpiredFiles() {
    List<FileInfo> expiredFiles = repo.findAllExpired();
    if (!expiredFiles.isEmpty()) {
      for (FileInfo info : expiredFiles) {
        try {
          Files.deleteIfExists(rootDir.resolve(info.getId()));
        } catch (IOException e) {
          LOG.error("Could not delete physical file " + info.getId(), e);
        }
      }
      repo.deleteAll(expiredFiles);
      return expiredFiles.size();
    }
    return 0;
  }

}
