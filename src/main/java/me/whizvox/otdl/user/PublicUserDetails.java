package me.whizvox.otdl.user;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PublicUserDetails {

  private Long id;

  private String email;

  private UserRank rank;

  private UserGroup group;

  private boolean enabled;

  public PublicUserDetails(User user) {
    setId(user.getId());
    setEmail(user.getEmail());
    setRank(user.getRank());
    setGroup(user.getGroup());
    setEnabled(user.isEnabled());
  }

}
