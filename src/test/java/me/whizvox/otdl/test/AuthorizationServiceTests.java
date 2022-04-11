package me.whizvox.otdl.test;

import me.whizvox.otdl.user.AuthorizationService;
import me.whizvox.otdl.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static me.whizvox.otdl.test.util.MockUser.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class AuthorizationServiceTests {

  private final AuthorizationService auth;

  @Autowired
  public AuthorizationServiceTests(AuthorizationService auth) {
    this.auth = auth;
  }

  @Test
  void hasPermission_givenRestrictedAndNeedUser_thenFalse() {
    assertThat(auth.hasPermission(restrictedUser(), "USER")).isFalse();
  }

  @Test
  void hasPermission_givenRestrictedAndNeedAdmin_thenFalse() {
    assertThat(auth.hasPermission(restrictedUser(), "ADMIN")).isFalse();
  }

  @Test
  void hasPermission_givenNullUserAndNeedUser_thenTrue() {
    assertThat(auth.hasPermission(null, "USER")).isFalse();
  }

  @Test
  void hasPermission_givenNullUserAndNeedAdmin_thenTrue() {
    assertThat(auth.hasPermission(null, "ADMIN")).isFalse();
  }

  @Test
  void hasPermission_givenUnverifiedUserAndNeedUser_thenFalse() {
    assertThat(auth.hasPermission(unverifiedMember(), "USER")).isFalse();
  }

  @Test
  void hasPermission_givenUnverifiedUserAndNeedAdmin_thenFalse() {
    assertThat(auth.hasPermission(unverifiedMember(), "ADMIN")).isFalse();
  }

  @Test
  void hasPermission_givenVerifiedUserAndNeedUser_thenTrue() {
    assertThat(auth.hasPermission(verifiedMember(), "USER")).isTrue();
  }

  @Test
  void hasPermission_givenVerifiedUserAndNeedAdmin_thenFalse() {
    assertThat(auth.hasPermission(verifiedMember(), "ADMIN")).isFalse();
  }

  @Test
  void hasPermission_givenContributorAndNeedUser_thenTrue() {
    assertThat(auth.hasPermission(contributor(), "USER")).isTrue();
  }

  @Test
  void hasPermission_givenContributorAndNeedAdmin_thenFalse() {
    assertThat(auth.hasPermission(contributor(), "ADMIN")).isFalse();
  }

  @Test
  void hasPermission_givenAdminAndNeedUser_thenTrue() {
    assertThat(auth.hasPermission(admin(), "USER")).isTrue();
  }

  @Test
  void hasPermission_givenAdminAndNeedAdmin_thenTrue() {
    assertThat(auth.hasPermission(admin(), "ADMIN")).isTrue();
  }

  @Test
  void canAccessUserDetails_givenMatchingUsers_thenTrue() {
    User u1 = restrictedUser();
    User u2 = unverifiedMember();
    User u3 = verifiedMember();
    User u4 = contributor();
    User u5 = admin();

    assertThat(auth.canAccessUserDetails(u1, u1.getId())).isTrue();
    assertThat(auth.canAccessUserDetails(u2, u2.getId())).isTrue();
    assertThat(auth.canAccessUserDetails(u3, u3.getId())).isTrue();
    assertThat(auth.canAccessUserDetails(u4, u4.getId())).isTrue();
    assertThat(auth.canAccessUserDetails(u5, u5.getId())).isTrue();
  }

  @Test
  void canAccessUserDetails_givenMismatchingNonAdminUsers_thenFalse() {
    User u1 = restrictedUser();
    User u2 = unverifiedMember();
    User u3 = verifiedMember();
    User u4 = contributor();
    User u5 = admin();

    assertThat(auth.canAccessUserDetails(null, u1.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(null, u2.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(null, u3.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(null, u4.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(null, u5.getId())).isFalse();

    assertThat(auth.canAccessUserDetails(u1, u2.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(u1, u3.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(u1, u4.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(u1, u5.getId())).isFalse();

    assertThat(auth.canAccessUserDetails(u2, u1.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(u2, u3.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(u2, u4.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(u2, u5.getId())).isFalse();

    assertThat(auth.canAccessUserDetails(u3, u1.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(u3, u2.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(u3, u4.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(u3, u5.getId())).isFalse();

    assertThat(auth.canAccessUserDetails(u4, u1.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(u4, u2.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(u4, u3.getId())).isFalse();
    assertThat(auth.canAccessUserDetails(u4, u5.getId())).isFalse();
  }

  @Test
  void canAccessUserDetails_givenAdminUser_thenTrue() {
    User u1 = restrictedUser();
    User u2 = unverifiedMember();
    User u3 = verifiedMember();
    User u4 = contributor();
    User u5 = admin();

    assertThat(auth.canAccessUserDetails(u5, u1.getId())).isTrue();
    assertThat(auth.canAccessUserDetails(u5, u2.getId())).isTrue();
    assertThat(auth.canAccessUserDetails(u5, u3.getId())).isTrue();
    assertThat(auth.canAccessUserDetails(u5, u4.getId())).isTrue();
  }
}
