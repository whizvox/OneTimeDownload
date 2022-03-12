package me.whizvox.otdl.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.TimeUnit;

public record ShutdownServerTask(ApplicationContext context) implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(ShutdownServerTask.class);

  @Override
  public void run() {
    LOG.warn("Shutting down server in 5 seconds...");
    try {
      TimeUnit.SECONDS.sleep(5);
    } catch (InterruptedException e) {
      LOG.error("Could not sleep", e);
    }
    ((ConfigurableApplicationContext) context).close();
  }

}
