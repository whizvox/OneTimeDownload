package me.whizvox.otdl.debug;

import me.whizvox.otdl.exception.OTDLServiceException;
import me.whizvox.otdl.file.FileRepository;
import me.whizvox.otdl.file.FileService;
import me.whizvox.otdl.task.ShutdownServerTask;
import me.whizvox.otdl.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/debug")
public class DebugController implements ApplicationContextAware {

  private static final Logger LOG = LoggerFactory.getLogger(DebugController.class);

  private final FileRepository fileRepo;
  private final FileService files;

  private ApplicationContext ctx;

  @Autowired
  public DebugController(FileRepository fileRepo, FileService files) {
    this.fileRepo = fileRepo;
    this.files = files;
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
      try {
        files.delete(info.getId());
        count.incrementAndGet();
      } catch (IOException e) {
        throw new OTDLServiceException(e);
      }
    });
    return ApiResponse.ok(count.get());
  }

  @PostMapping("server/shutdown")
  public ResponseEntity<Object> shutdown() {
    return Optional.ofNullable(ctx).map(ctx -> {
      new SimpleAsyncTaskExecutor().execute(new ShutdownServerTask(ctx));
      return ApiResponse.ok();
    }).orElse(ApiResponse.internalServerError("Application context unavailable"));
  }

}
