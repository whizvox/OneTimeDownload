package me.whizvox.otdl.test;

import me.whizvox.otdl.file.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FileControllerTests {

  private final FileController controller;
  private final FileRepository repo;
  private final FileService files;
  private final FileConfiguration config;
  private final MockMvc mvc;
  private final Path rootDir;

  @Autowired
  public FileControllerTests(FileController controller, FileRepository repo, FileService service, FileConfiguration config, MockMvc mvc) {
    this.controller = controller;
    this.repo = repo;
    this.files = service;
    this.config = config;
    rootDir = Paths.get(config.getUploadedFilesDirectory());
    this.mvc = mvc;
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
    info.setAuthToken("e87ced92fe7affc86d1fcc394bda654c28103567855a4513df65");
    info.setExpires(info.getUploaded().plusMinutes(60));
    repo.save(info);
    try (InputStream in = FileServiceTests.class.getClassLoader().getResourceAsStream("test/R1DZ4vpu966g")) {
      Files.copy(in, rootDir.resolve("R1DZ4vpu966g"));
    }
  }

  @AfterEach
  void tearDown() throws Exception {
    files.delete("R1DZ4vpu966g");
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
  void contextLoads() {
    assertThat(controller).isNotNull();
  }

  @Test
  void getInfo_givenKnownIdAndCorrectPassword_thenOk() throws Exception {
    mvc.perform(get("/files/info/R1DZ4vpu966g").param("password", "cGFzc3dvcmQxMjM"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false,\"data\":{\"id\":\"R1DZ4vpu966g\",\"uploaded\":\"2022-02-19T12:00:00\",\"originalSize\":445,\"expires\":\"2022-02-19T13:00:00\"}}")
        );
  }

  @Test
  void getInfo_givenUnknownIdAndCorrectPassword_thenNotFound() throws Exception {
    mvc.perform(get("/files/info/Z1DZ4vpu966g").param("password", "cGFzc3dvcmQxMjM"))
        .andDo(print())
        .andExpectAll(
            status().isNotFound(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":404,\"error\":true,\"data\":{\"message\":\"Resource not found\",\"path\":\"Z1DZ4vpu966g\"}}")
        );
  }

  @Test
  void getInfo_givenKnownIdAndNoPassword_thenUnauthorized() throws Exception {
    mvc.perform(get("/files/info/Z1DZ4vpu966g"))
        .andDo(print())
        .andExpectAll(
            status().isUnauthorized(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":401,\"error\":true,\"data\":{\"message\":\"Unauthorized\"}}")
        );
  }

  @Test
  void getInfo_givenUnknownIdAndNoPassword_thenUnauthorized() throws Exception {
    mvc.perform(get("/files/info/R1DZ4vpu966g"))
        .andDo(print())
        .andExpectAll(
            status().isUnauthorized(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":401,\"error\":true,\"data\":{\"message\":\"Unauthorized\"}}")
        );
  }

  @Test
  void getInfo_givenKnownIdAndIncorrectPassword_thenFakeNotFound() throws Exception {
    mvc.perform(get("/files/info/R1DZ4vpu966g").param("password", "somethingweird"))
        .andDo(print())
        .andExpectAll(
            status().isNotFound(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":404,\"error\":true,\"data\":{\"message\":\"Resource not found\",\"path\":\"R1DZ4vpu966g\"}}")
        );
  }

  @Test
  void download_givenValidIdAndCorrectPassword_thenOk() throws Exception {
    mvc.perform(get("/files/dl/R1DZ4vpu966g").param("password", "cGFzc3dvcmQxMjM"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_OCTET_STREAM),
            content().string("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
        );
  }

  @Test
  void download_givenUnknownId_thenNotFound() throws Exception {
    mvc.perform(get("/files/dl/G1DZ4vpu966g").param("password", "cGFzc3dvcmQxMjM"))
        .andDo(print())
        .andExpectAll(
            status().isNotFound(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":404,\"error\":true,\"data\":{\"message\":\"Resource not found\",\"path\":\"G1DZ4vpu966g\"}}")
        );
  }

  @Test
  void download_givenNoPassword_thenUnauthorized() throws Exception {
    mvc.perform(get("/files/dl/R1DZ4vpu966g"))
        .andDo(print())
        .andExpectAll(
            status().isUnauthorized(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":401,\"error\":true,\"data\":{\"message\":\"Unauthorized\"}}")
        );
  }

  @Test
  void download_givenIncorrectPassword_thenUnauthorized() throws Exception {
    mvc.perform(get("/files/dl/R1DZ4vpu966g").param("password", "somethingweird"))
        .andDo(print())
        .andExpectAll(
            status().isUnauthorized(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":401,\"error\":true,\"data\":{\"message\":\"Unauthorized\"}}")
        );
  }

  @Test
  void upload_givenValidParameters_thenOk() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON)
        );
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
                .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
                .param("password", "cGFzc3dvcmQxMjM")
                .param("lifespan", "30"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON)
        );
  }

  @Test
  void upload_givenMinAndMaxLifespan_thenOk() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
                .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
                .param("password", "cGFzc3dvcmQxMjM")
                .param("lifespan", "1"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false,\"data\":{\"originalSize\":17}}")
        );
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
                .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
                .param("password", "cGFzc3dvcmQxMjM")
                .param("lifespan", Integer.toString(config.getMaxLifespanMember())))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false,\"data\":{\"originalSize\":17}}")
        );
  }

  @Test
  void upload_givenInvalidLifespan_thenBadRequest() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
                .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
                .param("password", "cGFzc3dvcmQxMjM")
                .param("lifespan", "0"))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":{\"message\":\"Bad lifespan: 0 minutes\"}}")
        );
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
                .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
                .param("password", "cGFzc3dvcmQxMjM")
                .param("lifespan", "-1"))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":{\"message\":\"Bad lifespan: -1 minutes\"}}")
        );
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
                .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
                .param("password", "cGFzc3dvcmQxMjM")
                .param("lifespan", Integer.toString(config.getMaxLifespanMember() + 1)))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":{\"message\":\"Bad lifespan: " + (config.getMaxLifespanMember() + 1) + " minutes\"}}")
        );
  }

  @Test
  void upload_givenNoFile_thenBadRequest() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .param("password", "cGFzc3dvcmQxMjM"))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":{\"message\":\"Missing file\"}}")
        );
  }

  @Test
  void upload_givenNoPassword_thenBadRequest() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8))))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":{\"message\":\"Missing password parameter\"}}")
        );
  }

  @Test
  void delete_givenValidIdAndCorrectPassword_thenOk() throws Exception {
    mvc.perform(delete("/files/R1DZ4vpu966g").param("password", "cGFzc3dvcmQxMjM"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false}")
        );
  }

  @Test
  void delete_givenKnownIdAndCorrectPassword_thenFileDeleted() throws Exception {
    mvc.perform(delete("/files/R1DZ4vpu966g").param("password", "cGFzc3dvcmQxMjM")).andDo(print());
    assertThat(files.getInfo("R1DZ4vpu966g")).isEmpty();
  }

  @Test
  void delete_givenUnknownIdAndSomePassword_thenOk() throws Exception {
    mvc.perform(delete("/files/Z1DZ4vpu966g").param("password", "cGFzc3dvcmQxMjM"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false}")
        );
  }

  @Test
  void delete_givenKnownIdAndNoPassword_thenUnauthorized() throws Exception {
    mvc.perform(delete("/files/R1DZ4vpu966g"))
        .andDo(print())
        .andExpectAll(
            status().isUnauthorized(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":401,\"error\":true,\"data\":{\"message\":\"Unauthorized\"}}")
        );
  }

  @Test
  void delete_givenUnknownIdAndNoPassword_thenUnauthorized() throws Exception {
    mvc.perform(delete("/files/Z1DZ4vpu966g"))
        .andDo(print())
        .andExpectAll(
            status().isUnauthorized(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":401,\"error\":true,\"data\":{\"message\":\"Unauthorized\"}}")
        );
  }

  @Test
  void delete_givenKnownIdIncorrectPassword_thenOk() throws Exception {
    mvc.perform(delete("/files/R1DZ4vpu966g").param("password", "somethingweird"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false}")
        );
  }

  @Test
  void delete_givenKnownIdIncorrectPassword_thenFileNotDeleted() throws Exception {
    mvc.perform(delete("/files/R1DZ4vpu966g").param("password", "somethingweird")).andDo(print());
    assertThat(files.getInfo("R1DZ4vpu966g")).isPresent();
  }

}