package me.whizvox.otdl.test.util;

import me.whizvox.otdl.user.User;

public class MockUser {

  // TODO Fix mock user properties

  public static User restrictedUser() {
    /*return User.builder()
        .id((long) 1)
        .email("restricted@example.com")
        .group(UserGroup.RESTRICTED)
        .rank(null)
        .enabled(true)
        .password("$2a$06$IhZWmIRmi8M9BMWUep2K8uGPY/iDphaDWMKZYHIK9ouYfyHJO906q")
        .build();*/
    return new User();
  }

  public static User unverifiedMember() {
    /*return User.builder()
        .id((long) 2)
        .email("unverified@example.com")
        .group(UserGroup.USER)
        .rank(UserRank.MEMBER)
        .enabled(false)
        .password("$2a$06$IhZWmIRmi8M9BMWUep2K8uGPY/iDphaDWMKZYHIK9ouYfyHJO906q")
        .build();*/
    return new User();
  }

  public static User verifiedMember() {
    /*return User.builder()
        .id((long) 3)
        .email("verified@example.com")
        .group(UserGroup.USER)
        .rank(UserRank.MEMBER)
        .enabled(true)
        .password("$2a$06$IhZWmIRmi8M9BMWUep2K8uGPY/iDphaDWMKZYHIK9ouYfyHJO906q")
        .build();*/
    return new User();
  }

  public static User contributor() {
    /*return User.builder()
        .id((long) 4)
        .email("contributor@example.com")
        .group(UserGroup.USER)
        .rank(UserRank.CONTRIBUTOR)
        .enabled(true)
        .password("$2a$06$IhZWmIRmi8M9BMWUep2K8uGPY/iDphaDWMKZYHIK9ouYfyHJO906q")
        .build();*/
    return new User();
  }

  public static User admin() {
    /*return User.builder()
        .id((long) 5)
        .email("admin@example.com")
        .group(UserGroup.ADMIN)
        .rank(null)
        .enabled(true)
        .password("$2a$06$IhZWmIRmi8M9BMWUep2K8uGPY/iDphaDWMKZYHIK9ouYfyHJO906q")
        .build();*/
    return new User();
  }

}
