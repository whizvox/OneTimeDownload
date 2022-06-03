package me.whizvox.otdl.user;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConfirmationTokenRepository extends CrudRepository<ConfirmationToken, UUID> {

  Optional<ConfirmationToken> findByToken(String token);

}
