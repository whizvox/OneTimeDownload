package me.whizvox.otdl.user;

import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthorizationService {

  public boolean hasPermission(User user, String group) {
    return user != null && user.getGroup().ordinal() >= UserGroup.valueOf(group).ordinal();
  }

  public boolean canAccessUserDetails(User user, Long id) {
    return user != null && (user.getGroup() == UserGroup.ADMIN || Objects.equals(user.getId(), id));
  }

}
