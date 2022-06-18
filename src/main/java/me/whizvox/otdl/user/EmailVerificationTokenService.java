package me.whizvox.otdl.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class EmailVerificationTokenService {

  private final EmailVerificationTokenRepository repo;

  @Autowired
  public EmailVerificationTokenService(EmailVerificationTokenRepository repo) {
    this.repo = repo;
  }

  public Optional<EmailVerificationToken> getTokenInfo(String token) {
    return repo.findByToken(token);
  }

  public void store(EmailVerificationToken token) {
    repo.save(token);
    log.info("Saving confirmation token {}", token.getId());
  }

  public void delete(UUID id) {
    repo.deleteById(id);
    log.info("Email verification token {} deleted", id);
  }

  public void deleteAllByUser(UUID userId) {
    repo.deleteAllByUser(userId);
  }

}
