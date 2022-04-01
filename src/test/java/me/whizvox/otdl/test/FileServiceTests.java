package me.whizvox.otdl.test;

import me.whizvox.otdl.exception.FileMismatchException;
import me.whizvox.otdl.exception.InvalidLifespanException;
import me.whizvox.otdl.exception.NoFileException;
import me.whizvox.otdl.exception.WrongPasswordException;
import me.whizvox.otdl.file.FileConfiguration;
import me.whizvox.otdl.file.FileInfo;
import me.whizvox.otdl.file.FileRepository;
import me.whizvox.otdl.file.FileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FileServiceTests {

  private final FileRepository repo;
  private final FileService files;

  private final Path rootDir;

  @Autowired
  public FileServiceTests(FileConfiguration config, FileRepository repo, FileService files) throws Exception {
    this.repo = repo;
    this.files = files;

    rootDir = Paths.get("files").toAbsolutePath();

    //files.upload(new MockMultipartFile("test.txt", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.".getBytes(StandardCharsets.UTF_8)), "password123".toCharArray());
  }

  private byte[] readResource(Resource res) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
         InputStream in = res.getInputStream()) {
      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
      return out.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    Files.createDirectories(rootDir);
    FileInfo info = new FileInfo();
    info.setId("R1DZ4vpu966g");
    info.setOriginalSize(445);
    info.setStoredSize(448);
    info.setMd5("db89bb5ceab87f9c0fcc2ab36c189c2c");
    info.setSha1("cd36b370758a259b34845084a6cc38473cb95e27");
    info.setUploaded(LocalDateTime.of(2022, 2, 19, 12, 0, 0));
    info.setExpires(info.getUploaded().plusMinutes(60));
    info.setDownloaded(false);
    info.setAuthToken("e87ced92fe7affc86d1fcc394bda654c28103567855a4513df65");
    repo.save(info);
    try (InputStream in = FileServiceTests.class.getClassLoader().getResourceAsStream("test/R1DZ4vpu966g")) {
      Files.copy(in, rootDir.resolve("R1DZ4vpu966g"));
    }
  }

  @AfterEach
  void tearDown() throws Exception {
    if (repo.existsById("R1DZ4vpu966g")) {
      repo.deleteById("R1DZ4vpu966g");
    }
    Files.walkFileTree(rootDir, new FileVisitor<>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
      }
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }
      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        exc.printStackTrace();
        return FileVisitResult.CONTINUE;
      }
      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  @Test
  void getInfo_givenExistingId_thenResult() {
    assertTrue(files.getInfo("R1DZ4vpu966g").isPresent());
  }

  @Test
  void getInfo_givenBadId_thenEmpty() {
    assertTrue(files.getInfo("############").isEmpty());
  }

  @Test
  void getInfo_givenExistingId_thenFieldsMatch() {
    FileInfo info = files.getInfo("R1DZ4vpu966g").get();
    assertEquals("R1DZ4vpu966g", info.getId());
    assertEquals(445, info.getOriginalSize());
    assertEquals(448, info.getStoredSize());
    assertEquals("db89bb5ceab87f9c0fcc2ab36c189c2c", info.getMd5());
    assertEquals("cd36b370758a259b34845084a6cc38473cb95e27", info.getSha1());
    assertEquals("e87ced92fe7affc86d1fcc394bda654c28103567855a4513df65", info.getAuthToken());
    assertEquals(LocalDateTime.of(2022, 2, 19, 12, 0, 0), info.getUploaded());
    assertEquals(LocalDateTime.of(2022, 2, 19, 13, 0, 0), info.getExpires());
    assertFalse(info.isDownloaded());
  }

  @Test
  void upload_givenGoodInput_thenSuccess() {
    assertDoesNotThrow(() -> {
      FileInfo info = files.upload(new MockMultipartFile("test.txt", "some content here".getBytes(StandardCharsets.UTF_8)), 60, "password123".toCharArray());
      assertEquals("f3cf3af6d97fa0040dafc8963d8f2c47", info.getMd5());
      assertEquals("ebc29fb09abaddf38513475fae93dc10e1be8afa", info.getSha1());
      assertEquals(17, info.getOriginalSize());
      assertEquals(24, info.getStoredSize());

      assertTrue(files.getInfo(info.getId()).isPresent());
    });
  }

  @Test
  void upload_givenZeroLengthPassword_thenSuccess() {
    assertDoesNotThrow(() -> files.upload(new MockMultipartFile("test.txt", "some content here".getBytes(StandardCharsets.UTF_8)), 60, new char[0]));
  }

  @Test
  void upload_givenZeroLengthFile_thenSuccess() {
    assertDoesNotThrow(() -> files.upload(new MockMultipartFile("test.txt", new byte[0]), 60, "password123".toCharArray()));
  }

  @Test
  void upload_givenNoFile_thenThrow() {
    assertThrows(NoFileException.class, () -> files.upload(null, 60, "password123".toCharArray()));
  }

  @Test
  void upload_givenMinLifespan_thenSuccess() {
    assertDoesNotThrow(() -> files.upload(new MockMultipartFile("test.txt", "some content here".getBytes(StandardCharsets.UTF_8)), 1, "password123".toCharArray()));
  }

  @Test
  void upload_givenZeroLifespan_thenThrow() {
    assertThrows(InvalidLifespanException.class, () -> files.upload(new MockMultipartFile("test.txt", "some content here".getBytes(StandardCharsets.UTF_8)), 0, "password123".toCharArray()));
  }

  @Test
  void upload_givenNegativeLifespan_thenThrow() {
    assertThrows(InvalidLifespanException.class, () -> files.upload(new MockMultipartFile("test.txt", "some content here".getBytes(StandardCharsets.UTF_8)), -1, "password123".toCharArray()));
  }

  @Test
  void serve_givenGoodInput_thenSuccess() {
    Resource res = files.serve("R1DZ4vpu966g", "password123".toCharArray(), true);
    String msg = new String(readResource(res), StandardCharsets.UTF_8);
    assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", msg);
  }

  @Test
  void serve_givenWrongPassword_thenThrow() {
    assertThrows(WrongPasswordException.class, () -> files.serve("R1DZ4vpu966g", "password124".toCharArray(), true));
  }

  @Test
  void serve_givenFileMismatch_thenThrow() throws Exception {
    Files.deleteIfExists(rootDir.resolve("R1DZ4vpu966g"));
    assertThrows(FileMismatchException.class, () -> files.serve("R1DZ4vpu966g", "password123".toCharArray(), true));
  }

  @Test
  void serve_givenBadId_thenNull() {
    assertNull(files.serve("############", "password123".toCharArray(), true));
  }

  @Test
  void serve_givenDownloadedFile_thenNull() {
    files.serve("R1DZ4vpu966g", "password123".toCharArray(), true);
    assertThat(files.serve("R1DZ4vpu966g", "password123".toCharArray(), true)).isNull();
  }

  @Test
  void delete_givenExistingFile_thenSuccess() throws Exception {
    files.delete("R1DZ4vpu966g");
    assertFalse(files.getInfo("R1DZ4vpu966g").isPresent());
  }

  @Test
  void deleteExpiredFiles_givenExpiredFile_thenDelete() {
    FileInfo info = repo.findById("R1DZ4vpu966g").get();
    info.setExpires(LocalDateTime.now().minusMinutes(1));
    repo.save(info);
    assertThat(files.deleteExpiredFiles()).isEqualTo(1);
    assertThat(repo.findById("R1DZ4vpu966g")).isEmpty();
  }

  @Test
  void deleteExpiredFiles_givenNoExpiredFiles_thenNoOp() {
    FileInfo info = repo.findById("R1DZ4vpu966g").get();
    info.setExpires(LocalDateTime.now().plusMinutes(1));
    repo.save(info);
    assertThat(files.deleteExpiredFiles()).isEqualTo(0);
    assertThat(repo.findById("R1DZ4vpu966g")).isPresent();
  }

}