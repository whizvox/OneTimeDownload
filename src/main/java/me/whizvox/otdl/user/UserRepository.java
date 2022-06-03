package me.whizvox.otdl.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends PagingAndSortingRepository<User, UUID>, JpaSpecificationExecutor<User> {

  Optional<User> findByEmail(String email);

  @Query(value = "SELECT COUNT(user) FROM users user WHERE user.verified=false")
  long countAllUnverified();

}
