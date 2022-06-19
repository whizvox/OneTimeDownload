package me.whizvox.otdl.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetTokenService {

  private PasswordResetTokenRepository repo;
  private UserConfigurationProperties props;

  @Autowired
  public PasswordResetTokenService(PasswordResetTokenRepository repo,
                                   UserConfigurationProperties props) {
    this.repo = repo;
    this.props = props;
  }

  public Optional<PasswordResetToken> getToken(UUID tokenId) {
    return repo.findById(tokenId).filter(token -> {
      if (token.getExpires().isBefore(LocalDateTime.now())) {
        repo.delete(token);
        return false;
      }
      return true;
    });
  }

  public PasswordResetToken create(User user) {
    deleteAllByUser(user.getId());
    return repo.save(PasswordResetToken.create(user, LocalDateTime.now().plusMinutes(props.getPasswordResetLinkLifetime())));
  }

  public void delete(UUID tokenId) {
    repo.deleteById(tokenId);
  }

  public void deleteAllByUser(UUID userId) {
    repo.deleteAllByUser(userId);
  }

  public int deleteAllExpired() {
    List<PasswordResetToken> allExpired = repo.findAllExpired();
    int count = allExpired.size();
    repo.deleteAll(allExpired);
    return count;
  }

}
