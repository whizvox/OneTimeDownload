package me.whizvox.otdl.test;

import me.whizvox.otdl.file.*;
import me.whizvox.otdl.storage.StorageService;
import me.whizvox.otdl.test.util.MockFile;
import me.whizvox.otdl.test.util.MockUser;
import me.whizvox.otdl.user.User;
import me.whizvox.otdl.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FileControllerTests {

  private final StorageService storage;
  private final FileController controller;
  private final FileRepository repo;
  private final FileService files;
  private final FileConfiguration config;
  private final UserRepository userRepo;
  private final MockMvc mvc;
  private final Path rootDir;

  private User restrictedUser;
  private User unverifiedMember;
  private User verifiedMember;
  private User contributor;
  private User admin;

  private FileInfo verifiedFile;
  private FileInfo contributorFile;

  @Autowired
  public FileControllerTests(StorageService storage, FileController controller, FileRepository repo, FileService service, FileConfiguration config, UserRepository userRepo, MockMvc mvc) {
    this.storage = storage;
    this.controller = controller;
    this.repo = repo;
    this.files = service;
    this.config = config;
    this.userRepo = userRepo;
    rootDir = Paths.get("files").toAbsolutePath().normalize();
    this.mvc = mvc;

    restrictedUser = unverifiedMember = verifiedMember = contributor = admin = null;
    verifiedFile = contributorFile = null;
  }

  @BeforeEach
  void setUp() throws Exception {
    restrictedUser = userRepo.save(MockUser.restrictedUser());
    unverifiedMember = userRepo.save(MockUser.unverifiedMember());
    verifiedMember = userRepo.save(MockUser.verifiedMember());
    contributor = userRepo.save(MockUser.contributor());
    admin = userRepo.save(MockUser.admin());

    Files.createDirectories(rootDir);

    repo.save(MockFile.asAnonymous());
    MockFile.copyFromResources(storage, MockFile.ANONYMOUS);

    verifiedFile = MockFile.asVerifiedMember();
    verifiedFile.setUser(verifiedMember);
    repo.save(verifiedFile);
    MockFile.copyFromResources(storage, MockFile.VERIFIED_MEMBER);

    contributorFile = MockFile.asContributor();
    contributorFile.setUser(contributor);
    repo.save(contributorFile);
    MockFile.copyFromResources(storage, MockFile.CONTRIBUTOR);
  }

  @AfterEach
  void tearDown() throws Exception {
    files.delete(StreamSupport.stream(repo.findAll().spliterator(), false).map(FileInfo::getId).collect(Collectors.toList()));
    userRepo.deleteAll();
    restrictedUser = unverifiedMember = verifiedMember = contributor = admin = null;
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
            content().json("{\"status\":200,\"error\":false,\"data\":{\"id\":\"R1DZ4vpu966g\",\"uploaded\":\"2022-02-19T12:00:00\",\"originalSize\":445,\"expires\":\"2022-02-19T12:30:00\"}}")
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
            content().json("{\"status\":401,\"error\":true,\"data\":\"Unauthorized\"}")
        );
  }

  @Test
  void getInfo_givenUnknownIdAndNoPassword_thenUnauthorized() throws Exception {
    mvc.perform(get("/files/info/R1DZ4vpu966g"))
        .andDo(print())
        .andExpectAll(
            status().isUnauthorized(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":401,\"error\":true,\"data\":\"Unauthorized\"}")
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
  void checkIfAvailable_givenKnownIdAndCorrectPassword_thenOkTrue() throws Exception {
    mvc.perform(get("/files/available/R1DZ4vpu966g").param("password", "cGFzc3dvcmQxMjM"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false,\"data\":true}")
        );
  }

  @Test
  void checkIfAvailable_givenUnknownId_thenOkFalse() throws Exception {
    mvc.perform(get("/files/available/G1DZ4vpu966g").param("password", "cGFzc3dvcmQxMjM"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false,\"data\":false}")
        );
  }

  @Test
  void checkIfAvailable_givenIncorrectPassword_thenOkFalse() throws Exception {
    mvc.perform(get("/files/available/G1DZ4vpu966g").param("password", "somethingblah"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false,\"data\":false}")
        );
  }

  @Test
  void download_givenValidIdAndCorrectPassword_thenOk() throws Exception {
    mvc.perform(get("/files/dl/R1DZ4vpu966g").param("password", "cGFzc3dvcmQxMjM"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_OCTET_STREAM),
            content().string("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."),
            header().string("content-disposition", "attachment; filename=\"test.txt\"")
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
            content().json("{\"status\":401,\"error\":true,\"data\":\"Unauthorized\"}")
        );
  }

  @Test
  void download_givenIncorrectPassword_thenUnauthorized() throws Exception {
    mvc.perform(get("/files/dl/R1DZ4vpu966g").param("password", "somethingweird"))
        .andDo(print())
        .andExpectAll(
            status().isUnauthorized(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":401,\"error\":true,\"data\":\"Unauthorized\"}")
        );
  }

  @Test
  void download_givenDownloadedFile_thenNotFound() throws Exception {
    mvc.perform(get("/files/dl/R1DZ4vpu966g").param("password", "cGFzc3dvcmQxMjM"));
    mvc.perform(get("/files/dl/R1DZ4vpu966g").param("password", "cGFzc3dvcmQxMjM"))
        .andDo(print())
        .andExpectAll(
            status().isNotFound(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":404,\"error\":true,\"data\":{\"message\":\"Resource not found\",\"path\":\"R1DZ4vpu966g\"}}")
        );
  }


  // --[ Anonymous user tests ]--

  @Test
  void upload_asAnonymous_givenValidParameters_thenOk() throws Exception {
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
  void upload_asAnonymous_givenMinAndMaxLifespan_thenOk() throws Exception {
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
                .param("lifespan", Integer.toString(config.getMaxLifespanGuest())))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false,\"data\":{\"originalSize\":17}}")
        );
  }

  @Test
  void upload_asAnonymous_givenInvalidLifespan_thenBadRequest() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
                .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
                .param("password", "cGFzc3dvcmQxMjM")
                .param("lifespan", "0"))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Bad lifespan (0): min 1, max 30 minutes\"}")
        );

    mvc.perform(MockMvcRequestBuilders.multipart("/files")
                .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
                .param("password", "cGFzc3dvcmQxMjM")
                .param("lifespan", "-1"))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Bad lifespan (-1): min 1, max 30 minutes\"}")
        );
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .param("lifespan", Integer.toString(config.getMaxLifespanGuest() + 1)))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Bad lifespan (31): min 1, max 30 minutes\"}")
        );
  }

  @Test
  void upload_asRestricted_thenForbidden() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .with(SecurityMockMvcRequestPostProcessors.user(restrictedUser)))
        .andDo(print())
        .andExpectAll(
            status().isForbidden(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":403,\"error\":true,\"data\":\"Account is restricted\"}")
        );
  }

  @Test
  void upload_asUnverifiedMember_thenForbidden() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .with(SecurityMockMvcRequestPostProcessors.user(unverifiedMember)))
        .andDo(print())
        .andExpectAll(
            status().isForbidden(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":403,\"error\":true,\"data\":\"Account email is unverified\"}")
        );
  }

  @Test
  void upload_asVerifiedMember_givenValidParameters_thenOk() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .with(SecurityMockMvcRequestPostProcessors.user(verifiedMember)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON)
        );
  }

  @Test
  void upload_asVerifiedMember_givenMinAndMaxLifespan_thenOk() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .param("lifespan", "1")
            .with(SecurityMockMvcRequestPostProcessors.user(verifiedMember)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON)
        );

    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .param("lifespan", Integer.toString(config.getMaxLifespanMember()))
            .with(SecurityMockMvcRequestPostProcessors.user(verifiedMember)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON)
        );
  }

  @Test
  void upload_asVerifiedMember_givenInvalidLifespan_thenBadRequest() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .param("lifespan", "0")
            .with(SecurityMockMvcRequestPostProcessors.user(verifiedMember)))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Bad lifespan (0): min 1, max 720 minutes\"}")
        );

    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .param("lifespan", "-1")
            .with(SecurityMockMvcRequestPostProcessors.user(verifiedMember)))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Bad lifespan (-1): min 1, max 720 minutes\"}")
        );

    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .param("lifespan", Integer.toString(config.getMaxLifespanMember() + 1))
            .with(SecurityMockMvcRequestPostProcessors.user(verifiedMember)))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Bad lifespan (721): min 1, max 720 minutes\"}")
        );
  }

  @Test
  void upload_asContributor_givenValidParameters_thenOk() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .with(SecurityMockMvcRequestPostProcessors.user(contributor)))
        .andDo(print())
        .andExpectAll(
          status().isOk()
        );
  }

  @Test
  void upload_asContributor_givenValidLifespan_thenOk() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .param("lifespan", "1")
            .with(SecurityMockMvcRequestPostProcessors.user(contributor)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON)
        );

    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .param("lifespan", Integer.toString(config.getMaxLifespanContributor()))
            .with(SecurityMockMvcRequestPostProcessors.user(contributor)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON)
        );
  }

  @Test
  void upload_asContributor_givenInvalidLifespan_thenBadRequest() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .param("lifespan", "0")
            .with(SecurityMockMvcRequestPostProcessors.user(contributor)))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Bad lifespan (0): min 1, max 14400 minutes\"}")
        );

    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .param("lifespan", "-1")
            .with(SecurityMockMvcRequestPostProcessors.user(contributor)))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Bad lifespan (-1): min 1, max 14400 minutes\"}")
        );

    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .param("lifespan", Integer.toString(config.getMaxLifespanContributor() + 1))
            .with(SecurityMockMvcRequestPostProcessors.user(contributor)))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Bad lifespan (14401): min 1, max 14400 minutes\"}")
        );
  }

  @Test
  void upload_asAdmin_givenMaxLifespan_thenOk() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .param("lifespan", String.valueOf(Integer.MAX_VALUE))
            .with(SecurityMockMvcRequestPostProcessors.user(admin)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON)
        );
  }

  @Test
  void upload_asAdmin_givenInvalidLifespan_thenBadRequest() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .param("lifespan", "0")
            .with(SecurityMockMvcRequestPostProcessors.user(admin)))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Bad lifespan (0): min 1, max 2147483647 minutes\"}")
        );

    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "cGFzc3dvcmQxMjM")
            .param("lifespan", "-1")
            .with(SecurityMockMvcRequestPostProcessors.user(admin)))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Bad lifespan (-1): min 1, max 2147483647 minutes\"}")
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
            content().json("{\"status\":400,\"error\":true,\"data\":\"Missing file\"}")
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
            content().json("{\"status\":400,\"error\":true,\"data\":\"Missing password\"}")
        );
  }

  @Test
  void upload_givenMalformedPassword_thenBadRequest() throws Exception {
    mvc.perform(MockMvcRequestBuilders.multipart("/files")
            .file(new MockMultipartFile("file", "some content here".getBytes(StandardCharsets.UTF_8)))
            .param("password", "invalid password"))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Invalid password base64 string\"}")
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
            content().json("{\"status\":401,\"error\":true,\"data\":\"Unauthorized\"}")
        );
  }

  @Test
  void delete_givenUnknownIdAndNoPassword_thenUnauthorized() throws Exception {
    mvc.perform(delete("/files/Z1DZ4vpu966g"))
        .andDo(print())
        .andExpectAll(
            status().isUnauthorized(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":401,\"error\":true,\"data\":\"Unauthorized\"}")
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

  /*@Test
  void search_asAnonymous_thenForbidden() throws Exception {
    mvc.perform(get("/files/search"))
        .andDo(print())
        .andExpectAll(
            status().isForbidden(),
            content().contentType(MediaType.APPLICATION_JSON)
        );
  }*/

  @Test
  void search_asAdmin_givenNoArguments_thenOk() throws Exception {
    mvc.perform(get("/files/search")
            .with(SecurityMockMvcRequestPostProcessors.user(admin)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false,\"data\":{\"count\":3,\"total\":3,\"pages\":1,\"items\":[{\"id\":\"7jfeMbtSCGxa\",\"fileName\":\"test2.txt\",\"authToken\":\"d09277d91504bc377cd85101c506ed9377b96d59e71e304b873e\",\"uploaded\":\"2022-02-19T14:00:00\",\"md5\":\"86fb269d190d2c85f6e0468ceca42a20\",\"sha1\":\"d3486ae9136e7856bc42212385ea797094475802\",\"originalSize\":12,\"storedSize\":16,\"expires\":\"2022-02-22T08:40:00\",\"downloaded\":false,\"user\":{\"email\":\"contributor@example.com\",\"password\":\"$2a$06$IhZWmIRmi8M9BMWUep2K8uGPY/iDphaDWMKZYHIK9ouYfyHJO906q\",\"rank\":\"CONTRIBUTOR\",\"group\":\"USER\",\"enabled\":true,\"accountNonExpired\":true,\"credentialsNonExpired\":true,\"accountNonLocked\":true,\"username\":\"contributor@example.com\",\"authorities\":[{\"authority\":\"ROLE_USER\"}],\"loggedIn\":true}},{\"id\":\"nOj8sn6xZ-ca\",\"fileName\":\"test1.txt\",\"authToken\":\"faca94dc79dcdbc415d186a9bce257231509c7ef5f234d8a1453\",\"uploaded\":\"2022-02-19T13:00:00\",\"md5\":\"86fb269d190d2c85f6e0468ceca42a20\",\"sha1\":\"d3486ae9136e7856bc42212385ea797094475802\",\"originalSize\":12,\"storedSize\":16,\"expires\":\"2022-02-19T14:30:00\",\"downloaded\":false,\"user\":{\"email\":\"verified@example.com\",\"password\":\"$2a$06$IhZWmIRmi8M9BMWUep2K8uGPY/iDphaDWMKZYHIK9ouYfyHJO906q\",\"rank\":\"MEMBER\",\"group\":\"USER\",\"enabled\":true,\"accountNonExpired\":true,\"credentialsNonExpired\":true,\"accountNonLocked\":true,\"username\":\"verified@example.com\",\"authorities\":[{\"authority\":\"ROLE_USER\"}],\"loggedIn\":true}},{\"id\":\"R1DZ4vpu966g\",\"fileName\":\"test.txt\",\"authToken\":\"e87ced92fe7affc86d1fcc394bda654c28103567855a4513df65\",\"uploaded\":\"2022-02-19T12:00:00\",\"md5\":\"db89bb5ceab87f9c0fcc2ab36c189c2c\",\"sha1\":\"cd36b370758a259b34845084a6cc38473cb95e27\",\"originalSize\":445,\"storedSize\":448,\"expires\":\"2022-02-19T12:30:00\",\"downloaded\":false,\"user\":null}]}}")
        );
  }

  @Test
  void search_asAdmin_givenPageArguments_thenOk() throws Exception {
    mvc.perform(get("/files/search")
            .with(SecurityMockMvcRequestPostProcessors.user(admin))
            .param("size", "1"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.data.index").value(0),
            jsonPath("$.data.count").value(1),
            jsonPath("$.data.total").value(3),
            jsonPath("$.data.pages").value(3)
        );

    mvc.perform(get("/files/search")
            .with(SecurityMockMvcRequestPostProcessors.user(admin))
            .param("size", "1")
            .param("page", "1"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.data.index").value(1),
            jsonPath("$.data.count").value(1),
            jsonPath("$.data.total").value(3),
            jsonPath("$.data.pages").value(3)
        );

    mvc.perform(get("/files/search")
            .with(SecurityMockMvcRequestPostProcessors.user(admin))
            .param("size", "1")
            .param("page", "5"))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.data.index").value(5),
            jsonPath("$.data.count").value(0),
            jsonPath("$.data.total").value(3),
            jsonPath("$.data.pages").value(3)
        );
  }

  @Test
  void update_asAnonymous_thenUnauthorized() throws Exception {
    mvc.perform(put("/files/" + MockFile.ANONYMOUS)
        .param("fileName", "newfilename.txt")
        .param("expires", "2022-02-19T13:30:00"))
        .andDo(print())
        .andExpectAll(
            status().isForbidden(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":403,\"error\":true,\"data\":\"Cannot modify file as anonymous user\"}")
        );
  }

  @Test
  void update_asAdmin_givenValidArguments_thenOk() throws Exception {
    mvc.perform(put("/files/" + MockFile.ANONYMOUS)
            .param("fileName", "newfilename.txt")
            .param("expires", "2022-02-19T13:30:00")
            .param("downloaded", "true")
            .with(SecurityMockMvcRequestPostProcessors.user(admin)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false,\"data\":null}")
        );

    FileInfo file = files.getInfo(MockFile.ANONYMOUS).get();
    assertThat(file.getFileName()).isEqualTo("newfilename.txt");
    assertThat(file.getExpires()).isEqualTo(LocalDateTime.of(2022, 2, 19, 13, 30, 0));
    assertThat(file.isDownloaded()).isTrue();
  }

  @Test
  void update_givenEmptyFileName_thenBadRequest() throws Exception {
    mvc.perform(put("/files/" + MockFile.ANONYMOUS)
            .param("fileName", "")
            .with(SecurityMockMvcRequestPostProcessors.user(admin)))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Bad file name, cannot be empty\"}")
        );
  }

  @Test
  void update_asNonAdminFileOwner_givenValidArguments_thenOk() throws Exception {
    mvc.perform(put("/files/" + MockFile.VERIFIED_MEMBER)
            .param("fileName", "coolfilename.txt")
            .param("expires", "2022-02-19T14:45:00")
            .with(SecurityMockMvcRequestPostProcessors.user(verifiedMember)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false,\"data\":null}")
        );

    FileInfo file = files.getInfo(MockFile.VERIFIED_MEMBER).get();
    assertThat(file.getFileName()).isEqualTo("coolfilename.txt");
    assertThat(file.getExpires()).isEqualTo(LocalDateTime.of(2022, 2, 19, 14, 45, 0));
  }

  @Test
  void update_asNonAdminFileOwner_trySetDownloaded_thenForbidden() throws Exception {
    mvc.perform(put("/files/" + MockFile.VERIFIED_MEMBER)
            .param("fileName", "newfilename.txt")
            .param("expires", "2022-02-19T14:45:00")
            .param("downloaded", "true")
            .with(SecurityMockMvcRequestPostProcessors.user(verifiedMember)))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Do not have sufficient permissions to set downloaded flag\"}")
        );
  }

  @Test
  void update_asNonAdminOtherUser_thenNotFound() throws Exception {
    mvc.perform(put("/files/" + MockFile.VERIFIED_MEMBER)
            .param("fileName", "newfilename.txt")
            .param("expires", "2022-02-19T14:45:00")
            .with(SecurityMockMvcRequestPostProcessors.user(contributor)))
        .andDo(print())
        .andExpectAll(
            status().isNotFound(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":404,\"error\":true,\"data\":{\"message\":\"Resource not found\",\"path\":\"nOj8sn6xZ-ca\"}}")
        );
  }

  @Test
  void update_asMemberOwner_givenInvalidExpiresValue_thenBadRequest() throws Exception {
    mvc.perform(put("/files/" + MockFile.VERIFIED_MEMBER)
            .param("fileName", "newfilename.txt")
            .param("expires", "2022-02-21T14:45:00")
            .with(SecurityMockMvcRequestPostProcessors.user(verifiedMember)))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Lifespan too long (2985), 720 minutes max\"}")
        );
  }

  @Test
  void update_asContributorOwner_givenInvalidExpiresValue_thenBadRequest() throws Exception {
    mvc.perform(put("/files/" + MockFile.CONTRIBUTOR)
            .param("fileName", "newfilename.txt")
            .param("expires", "2022-04-19T14:45:00")
            .with(SecurityMockMvcRequestPostProcessors.user(contributor)))
        .andDo(print())
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":400,\"error\":true,\"data\":\"Lifespan too long (85005), 14400 minutes max\"}")
        );
  }

  @Test
  void getOwnFiles_asAnonymous_thenUnauthorized() throws Exception {
    mvc.perform(get("/files/all"))
        .andDo(print())
        .andExpectAll(
            status().isUnauthorized(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":401,\"error\":true,\"data\":\"Unauthorized\"}")
        );
  }

  @Test
  void getOwnFiles_thenOk() throws Exception {
    mvc.perform(get("/files/all").with(SecurityMockMvcRequestPostProcessors.user(verifiedMember)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.data.index").value(0),
            jsonPath("$.data.count").value(1),
            jsonPath("$.data.total").value(1),
            jsonPath("$.data.pages").value(1),
            jsonPath("$.data.items.length()").value(1),
            jsonPath("$.data.items[0].id").value(MockFile.VERIFIED_MEMBER)
        );

    mvc.perform(get("/files/all").with(SecurityMockMvcRequestPostProcessors.user(contributor)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.data.index").value(0),
            jsonPath("$.data.count").value(1),
            jsonPath("$.data.total").value(1),
            jsonPath("$.data.pages").value(1),
            jsonPath("$.data.items.length()").value(1),
            jsonPath("$.data.items[0].id").value(MockFile.CONTRIBUTOR)
        );

    mvc.perform(get("/files/all").with(SecurityMockMvcRequestPostProcessors.user(admin)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.data.index").value(0),
            jsonPath("$.data.count").value(0),
            jsonPath("$.data.total").value(0),
            jsonPath("$.data.pages").value(0),
            jsonPath("$.data.items.length()").value(0)
        );
  }

  //@Test
  void deleteBulk_asNonAdmin_thenForbidden() throws Exception {
    mvc.perform(post("/files/delete")
            .param("ids", MockFile.ANONYMOUS, MockFile.VERIFIED_MEMBER, MockFile.CONTRIBUTOR))
        .andDo(print())
        .andExpectAll(
            status().isForbidden(),
            content().contentType(MediaType.APPLICATION_JSON)
        );
  }

  @Test
  void deleteBulk_asAdmin_thenOk() throws Exception {
    mvc.perform(post("/files/delete")
            .param("ids", MockFile.ANONYMOUS, MockFile.VERIFIED_MEMBER)
            .with(SecurityMockMvcRequestPostProcessors.user(admin)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false,\"data\":null}")
        );

    assertThat(repo.findById(MockFile.ANONYMOUS)).isEmpty();
    assertThat(repo.findById(MockFile.VERIFIED_MEMBER)).isEmpty();
    assertThat(repo.findById(MockFile.CONTRIBUTOR)).isPresent();

    mvc.perform(post("/files/delete")
            .param("ids", "badid1", "badid2", "badid3")
            .with(SecurityMockMvcRequestPostProcessors.user(admin)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false,\"data\":null}")
        );

    assertThat(repo.findById(MockFile.CONTRIBUTOR)).isPresent();

    mvc.perform(post("/files/delete")
            .param("ids", MockFile.CONTRIBUTOR, "badid4")
            .with(SecurityMockMvcRequestPostProcessors.user(admin)))
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json("{\"status\":200,\"error\":false,\"data\":null}")
        );

    assertThat(repo.findById(MockFile.CONTRIBUTOR)).isEmpty();
  }

}