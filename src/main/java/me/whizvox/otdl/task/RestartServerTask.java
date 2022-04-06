package me.whizvox.otdl.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.devtools.restart.Restarter;

@Slf4j
public class RestartServerTask implements Runnable {

  @Override
  public void run() {
    log.warn("Restarting server in 5 seconds!");
    try {
      Thread.sleep(5000);
    } catch (InterruptedException ignored) {}
    Restarter.getInstance().restart();
  }

}
