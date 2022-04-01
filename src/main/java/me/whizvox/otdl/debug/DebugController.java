package me.whizvox.otdl.debug;

import me.whizvox.otdl.exception.OTDLServiceException;
import me.whizvox.otdl.file.FileRepository;
import me.whizvox.otdl.file.FileService;
import me.whizvox.otdl.task.ShutdownServerTask;
import me.whizvox.otdl.user.*;
import me.whizvox.otdl.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/debug")
public class DebugController implements ApplicationContextAware {

  private static final Logger LOG = LoggerFactory.getLogger(DebugController.class);

  private final FileRepository fileRepo;
  private final FileService files;
  private final UserRepository userRepo;
  private final ConfirmationTokenRepository tokenRepo;
  private final PasswordEncoder passwordEncoder;

  private ApplicationContext ctx;

  @Autowired
  public DebugController(FileRepository fileRepo, FileService files, UserRepository userRepo, ConfirmationTokenRepository tokenRepo, PasswordEncoder passwordEncoder) {
    this.fileRepo = fileRepo;
    this.files = files;
    this.userRepo = userRepo;
    this.tokenRepo = tokenRepo;
    this.passwordEncoder = passwordEncoder;
    ctx = null;
  }

  @Override
  public void setApplicationContext(ApplicationContext ctx) throws BeansException {
    this.ctx = ctx;
  }

  @DeleteMapping("files/clear")
  public ResponseEntity<Object> clearFiles() {
    LOG.info("Deleting {} files...", fileRepo.count());
    AtomicInteger count = new AtomicInteger(0);
    fileRepo.findAll().forEach(info -> {
        files.delete(info.getId());
        count.incrementAndGet();
    });
    return ApiResponse.ok(count.get());
  }

  @DeleteMapping("users/clear")
  public ResponseEntity<Object> clearUsers(@AuthenticationPrincipal User user) {
    long tokensCount = tokenRepo.count();
    tokenRepo.deleteAll();
    long usersCount = userRepo.count();
    userRepo.findAll().forEach(inUser -> {
      if (!Objects.equals(inUser.getId(), user.getId())) {
        userRepo.deleteById(inUser.getId());
      }
    });
    return ApiResponse.ok(new long[] {tokensCount, usersCount});
  }

  @PostMapping("users/admin")
  public ResponseEntity<Object> addAdmin() {
    if (userRepo.findByEmail("admin").isPresent()) {
      return ApiResponse.badRequest("Admin already present");
    }
    SecureRandom rand = new SecureRandom();
    byte[] bytes = new byte[15];
    rand.nextBytes(bytes);
    String password = Base64.getUrlEncoder().encodeToString(bytes);
    userRepo.save(User.builder().email("admin").password(passwordEncoder.encode(password)).group(UserGroup.ADMIN).enabled(true).build());
    return ApiResponse.ok(password);
  }

  @DeleteMapping("users/admin")
  public ResponseEntity<Object> deleteAdmin() {
    return userRepo.findByEmail("admin").map(user -> {
      userRepo.deleteById(user.getId());
      return ApiResponse.ok();
    }).orElse(ApiResponse.notFound(null));
  }

  @DeleteMapping("tokens/clear")
  public ResponseEntity<Object> clearConfirmationTokens() {
    long count = tokenRepo.count();
    tokenRepo.deleteAll();
    return ApiResponse.ok(count);
  }

  @PostMapping("server/shutdown")
  public ResponseEntity<Object> shutdown() {
    return Optional.ofNullable(ctx).map(ctx -> {
      new SimpleAsyncTaskExecutor().execute(new ShutdownServerTask(ctx));
      return ApiResponse.ok();
    }).orElse(ApiResponse.internalServerError("Application context unavailable"));
  }

}
