package me.whizvox.otdl.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, UUID> {

  @Query("SELECT token FROM PasswordResetToken token WHERE token.expires < current_timestamp")
  List<PasswordResetToken> findAllExpired();

  @Query("SELECT token FROM PasswordResetToken token WHERE token.user.id = :userId")
  List<PasswordResetToken> findAllForUser(UUID userId);

}
