package me.whizvox.otdl.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ConfirmationTokenService {

  private final ConfirmationTokenRepository repo;

  @Autowired
  public ConfirmationTokenService(ConfirmationTokenRepository repo) {
    this.repo = repo;
  }

  public Optional<ConfirmationToken> getTokenInfo(String token) {
    return repo.findByToken(token);
  }

  public void store(ConfirmationToken token) {
    repo.save(token);
    log.info("Saving confirmation token {}", token.getId());
  }

  public void delete(UUID id) {
    repo.deleteById(id);
    log.info("Confirmation token {} deleted", id);
  }

}
