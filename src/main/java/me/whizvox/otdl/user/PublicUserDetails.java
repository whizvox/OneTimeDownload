package me.whizvox.otdl.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
public class PublicUserDetails {

  private UUID id;

  private String email;

  private UserRole role;

  private LocalDateTime created;

  private boolean verified;

  public PublicUserDetails(User user) {
    setId(user.getId());
    setEmail(user.getEmail());
    setRole(user.getRole());
    setCreated(user.getCreated());
    setVerified(user.isVerified());
  }

}
