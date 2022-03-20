package me.whizvox.otdl.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
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
  }

  public void delete(Long id) {
    repo.deleteById(id);
  }

}
