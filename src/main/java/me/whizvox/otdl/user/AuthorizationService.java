package me.whizvox.otdl.user;

import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class AuthorizationService {

  public boolean hasPermission(User user, String role) {
    return user != null && user.isVerified() && user.getRole().ordinal() >= UserRole.valueOf(role).ordinal();
  }

  public boolean canAccessUserDetails(User user, UUID id) {
    return user != null && (user.getRole() == UserRole.ADMIN || Objects.equals(user.getId(), id));
  }

}
