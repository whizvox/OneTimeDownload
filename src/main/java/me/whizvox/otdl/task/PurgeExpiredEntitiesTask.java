package me.whizvox.otdl.task;

import me.whizvox.otdl.file.FileService;
import me.whizvox.otdl.user.EmailVerificationTokenService;
import me.whizvox.otdl.user.PasswordResetTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PurgeExpiredEntitiesTask {

  private static final Logger LOG = LoggerFactory.getLogger(PurgeExpiredEntitiesTask.class);

  private FileService files;
  private EmailVerificationTokenService confirmationTokens;
  private PasswordResetTokenService passwordResetTokens;

  public PurgeExpiredEntitiesTask(FileService files,
                                  EmailVerificationTokenService confirmationTokens,
                                  PasswordResetTokenService passwordResetTokens) {
    this.files = files;
    this.confirmationTokens = confirmationTokens;
    this.passwordResetTokens = passwordResetTokens;
  }

  @Scheduled(fixedDelay = 20, timeUnit = TimeUnit.SECONDS)
  public void work() {
    LOG.debug("Start purge expired files work cycle");
    int count = files.deleteExpiredFiles();
    if (count > 0) {
      LOG.info("{} file(s) deleted", count);
    }
    // TODO Purge expired verification tokens
    count = passwordResetTokens.deleteAllExpired();
    if (count > 0) {
      LOG.info("{} password reset token(s) deleted", count);
    }
  }

}
