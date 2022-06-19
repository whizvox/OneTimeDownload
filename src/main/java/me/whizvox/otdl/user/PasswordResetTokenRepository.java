package me.whizvox.otdl.user;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, UUID> {

  @Query("SELECT token FROM PasswordResetToken token WHERE token.expires < current_timestamp")
  List<PasswordResetToken> findAllExpired();

  @Modifying
  @Transactional
  @Query("DELETE FROM PasswordResetToken token WHERE token.user IS NOT NULL AND token.user.id = :userId")
  int deleteAllByUser(UUID userId);

}
