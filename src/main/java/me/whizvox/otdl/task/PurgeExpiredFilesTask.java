package me.whizvox.otdl.task;

import me.whizvox.otdl.file.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PurgeExpiredFilesTask {

  private static final Logger LOG = LoggerFactory.getLogger(PurgeExpiredFilesTask.class);

  private FileService files;

  public PurgeExpiredFilesTask(FileService files) {
    this.files = files;
  }

  @Scheduled(fixedDelay = 20, timeUnit = TimeUnit.SECONDS)
  public void work() {
    LOG.debug("Start purge expired files work cycle");
    int count = files.deleteExpiredFiles();
    if (count == 0) {
      LOG.debug("No files deleted");
    } else {
      LOG.info(count + " file(s) deleted");
    }
  }

}
