package me.whizvox.otdl.user;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends CrudRepository<EmailVerificationToken, UUID> {

  Optional<EmailVerificationToken> findByToken(String token);

  @Modifying
  @Transactional
  @Query("DELETE FROM EmailVerificationToken token WHERE token.user.id = :userId")
  void deleteAllByUser(UUID userId);

}
