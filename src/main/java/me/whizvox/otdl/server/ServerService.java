package me.whizvox.otdl.server;

import me.whizvox.otdl.file.FileService;
import me.whizvox.otdl.task.RestartServerTask;
import me.whizvox.otdl.task.ShutdownServerTask;
import me.whizvox.otdl.user.ConfirmationTokenService;
import me.whizvox.otdl.user.UserService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ServerService implements ApplicationContextAware {

  private FileService files;
  private UserService users;
  private ConfirmationTokenService tokens;
  private LocalDateTime startTime;
  private ApplicationContext ctx;

  @Autowired
  public ServerService(FileService files, UserService users, ConfirmationTokenService tokens) {
    this.files = files;
    this.users = users;
    this.tokens = tokens;
    startTime = LocalDateTime.now();
    ctx = null;
  }

  public ServerStatistics getStats() {
    ServerStatistics stats = new ServerStatistics();
    stats.setFilesCount(files.getCount());
    stats.setFilesStorage(files.getStorageUsed());
    stats.setUsersCount(users.getCount());
    stats.setUsersUnverified(users.getUnverifiedCount());
    stats.setServerUptime(getUptime().toMillis());
    return stats;
  }

  public Duration getUptime() {
    return Duration.between(startTime, LocalDateTime.now());
  }

  public void shutdown() {
    if (ctx == null) {
      throw new IllegalStateException("Application context has not been set");
    }
    new SimpleAsyncTaskExecutor().execute(new ShutdownServerTask(ctx));
  }

  public void restart() {
    new SimpleAsyncTaskExecutor().execute(new RestartServerTask());
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ctx = applicationContext;
  }

}
