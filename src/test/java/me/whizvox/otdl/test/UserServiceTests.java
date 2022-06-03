package me.whizvox.otdl.test;

import me.whizvox.otdl.exception.EmailTakenException;
import me.whizvox.otdl.exception.InvalidPasswordException;
import me.whizvox.otdl.test.util.MockUser;
import me.whizvox.otdl.user.*;
import me.whizvox.otdl.util.params.UpdateUserParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTests {

  private final MockMvc mvc;
  private final UserRepository repo;
  private final UserService users;

  private UUID restrictedUserId, unverifiedMemberId, verifiedMemberId, contributorId, adminId;

  @Autowired
  public UserServiceTests(MockMvc mvc, UserRepository repo, UserService users) {
    this.mvc = mvc;
    this.repo = repo;
    this.users = users;

    restrictedUserId = unverifiedMemberId = verifiedMemberId = contributorId = adminId = UUID.fromString("afcfd6d2-2960-40ce-bfe5-bd539e7cde3a");
  }

  @BeforeEach
  void setUp() {
    restrictedUserId = repo.save(MockUser.restrictedUser()).getId();
    unverifiedMemberId = repo.save(MockUser.unverifiedMember()).getId();
    verifiedMemberId = repo.save(MockUser.verifiedMember()).getId();
    contributorId = repo.save(MockUser.contributor()).getId();
    adminId = repo.save(MockUser.admin()).getId();
  }

  @AfterEach
  void tearDown() {
    repo.deleteAll();
  }

  @Test
  void getUserDetails_givenKnownId_thenPresentAndEqual() {
    Optional<User> u1 = users.getUserDetails(restrictedUserId);
    assertThat(u1).isPresent();
    User u1Expect = MockUser.restrictedUser();
    u1Expect.setId(restrictedUserId);
    assertThat(u1).hasValue(u1Expect);

    Optional<User> u2 = users.getUserDetails(unverifiedMemberId);
    assertThat(u2).isPresent();
    User u2Expect = MockUser.unverifiedMember();
    u2Expect.setId(unverifiedMemberId);
    assertThat(u2).hasValue(u2Expect);

    Optional<User> u3 = users.getUserDetails(verifiedMemberId);
    assertThat(u3).isPresent();
    User u3Expect = MockUser.verifiedMember();
    u3Expect.setId(verifiedMemberId);
    assertThat(u3).hasValue(u3Expect);

    Optional<User> u4 = users.getUserDetails(contributorId);
    assertThat(u4).isPresent();
    User u4Expect = MockUser.contributor();
    u4Expect.setId(contributorId);
    assertThat(u4).hasValue(u4Expect);

    Optional<User> u5 = users.getUserDetails(adminId);
    assertThat(u5).isPresent();
    User u5Expect = MockUser.admin();
    u5Expect.setId(adminId);
    assertThat(u5).hasValue(u5Expect);
  }

  @Test
  void getUserDetails_givenUnknownId_thenEmpty() {
    /*assertThat(users.getUserDetails(restrictedUserId - 1)).isEmpty();
    assertThat(users.getUserDetails(adminId + 1)).isEmpty();*/
  }

  @Test
  void loadUserByUsername_givenKnownEmail_thenSuccess() {
    UserDetails u1 = users.loadUserByUsername("restricted@example.com");
    User u1Expect = MockUser.restrictedUser();
    u1Expect.setId(restrictedUserId);
    assertThat(u1).isEqualTo(u1Expect);

    UserDetails u2 = users.loadUserByUsername("unverified@example.com");
    User u2Expect = MockUser.unverifiedMember();
    u2Expect.setId(unverifiedMemberId);
    assertThat(u2).isEqualTo(u2Expect);

    UserDetails u3 = users.loadUserByUsername("verified@example.com");
    User u3Expect = MockUser.verifiedMember();
    u3Expect.setId(verifiedMemberId);
    assertThat(u3).isEqualTo(u3Expect);

    UserDetails u4 = users.loadUserByUsername("contributor@example.com");
    User u4Expect = MockUser.contributor();
    u4Expect.setId(contributorId);
    assertThat(u4).isEqualTo(u4Expect);

    UserDetails u5 = users.loadUserByUsername("admin@example.com");
    User u5Expect = MockUser.admin();
    u5Expect.setId(adminId);
    assertThat(u5).isEqualTo(u5Expect);
  }

  @Test
  void loadUserByUsername_givenUnknownEmail_thenThrow() {
    assertThatThrownBy(() -> users.loadUserByUsername("unknown")).isInstanceOf(UsernameNotFoundException.class);
    assertThatThrownBy(() -> users.loadUserByUsername("UNVERIFIED_@example.com")).isInstanceOf(UsernameNotFoundException.class);
    assertThatThrownBy(() -> users.loadUserByUsername("admin@example.co")).isInstanceOf(UsernameNotFoundException.class);
  }

  @Test
  void isEmailAvailable_givenUnknownEmail_thenTrue() {
    assertThat(users.isEmailAvailable("admin2@example.com")).isTrue();
    assertThat(users.isEmailAvailable("verified2@example.com")).isTrue();
    assertThat(users.isEmailAvailable("unverified@example2.com")).isTrue();
  }

  @Test
  void isEmailAvailable_givenKnownEmail_thenFalse() throws Exception {
    assertThat(users.isEmailAvailable("restricted@example.com")).isFalse();
    assertThat(users.isEmailAvailable("unverified@example.com")).isFalse();
    assertThat(users.isEmailAvailable("verified@example.com")).isFalse();
    assertThat(users.isEmailAvailable("contributor@example.com")).isFalse();
    assertThat(users.isEmailAvailable("admin@example.com")).isFalse();
  }

  @Test
  void registerNewUser_givenValidArguments_thenSuccess() {
    User user = users.registerNewUser("available@example.com", "password123");
    assertThat(user.getEmail()).isEqualTo("available@example.com");
    assertThat(user.getPassword()).isNotEqualTo("password123");
    /*assertThat(user.getId()).isGreaterThan(0);
    assertThat(user.getGroup()).isEqualTo(UserGroup.USER);
    assertThat(user.getRank()).isEqualTo(UserRank.MEMBER);*/
  }

  @Test
  void registerNewUser_givenInvalidPassword_thenThrow() {
    assertThatThrownBy(() -> users.registerNewUser("available@example.com", "")).isInstanceOf(InvalidPasswordException.class);
    assertThatThrownBy(() -> users.registerNewUser("available@example.com", "a")).isInstanceOf(InvalidPasswordException.class);
    assertThatThrownBy(() -> users.registerNewUser("available@example.com", "aa")).isInstanceOf(InvalidPasswordException.class);
    assertThatThrownBy(() -> users.registerNewUser("available@example.com", "aaa")).isInstanceOf(InvalidPasswordException.class);
    assertThatThrownBy(() -> users.registerNewUser("available@example.com", "aaaa")).isInstanceOf(InvalidPasswordException.class);
    assertThatThrownBy(() -> users.registerNewUser("available@example.com", "aaaaa")).isInstanceOf(InvalidPasswordException.class);
    assertThatThrownBy(() -> users.registerNewUser("available@example.com", "aaaaaa")).isInstanceOf(InvalidPasswordException.class);
    assertThatThrownBy(() -> users.registerNewUser("available@example.com", "aaaaaaa")).isInstanceOf(InvalidPasswordException.class);
  }

  @Test
  void registerNewUser_givenTakenEmail_thenThrow() {
    assertThatThrownBy(() -> users.registerNewUser("restricted@example.com", "password123")).isInstanceOf(EmailTakenException.class);
    assertThatThrownBy(() -> users.registerNewUser("unverified@example.com", "password123")).isInstanceOf(EmailTakenException.class);
    assertThatThrownBy(() -> users.registerNewUser("verified@example.com", "password123")).isInstanceOf(EmailTakenException.class);
    assertThatThrownBy(() -> users.registerNewUser("contributor@example.com", "password123")).isInstanceOf(EmailTakenException.class);
    assertThatThrownBy(() -> users.registerNewUser("admin@example.com", "password123")).isInstanceOf(EmailTakenException.class);
  }

  @Test
  void createUser_givenValidArguments_thenSuccess() {
    /*User user = users.createUser("available@example.com", "password123", UserRank.CONTRIBUTOR, UserGroup.USER, true);
    assertThat(user.getEmail()).isEqualTo("available@example.com");
    assertThat(user.getPassword()).isNotEqualTo("password123");
    assertThat(user.getRank()).isEqualTo(UserRank.CONTRIBUTOR);
    assertThat(user.getGroup()).isEqualTo(UserGroup.USER);
    assertThat(user.isVerified()).isTrue();
    assertThat(user.getId()).isGreaterThan(0);*/
  }

  @Test
  void createUser_givenTakenEmail_thenThrow() {
    /*assertThatThrownBy(() -> users.createUser("restricted@example.com", "password123", UserRank.MEMBER, UserGroup.USER, true)).isInstanceOf(EmailTakenException.class);
    assertThatThrownBy(() -> users.createUser("unverified@example.com", "password123", UserRank.MEMBER, UserGroup.USER, true)).isInstanceOf(EmailTakenException.class);
    assertThatThrownBy(() -> users.createUser("verified@example.com", "password123", UserRank.MEMBER, UserGroup.USER, true)).isInstanceOf(EmailTakenException.class);
    assertThatThrownBy(() -> users.createUser("contributor@example.com", "password123", UserRank.MEMBER, UserGroup.USER, true)).isInstanceOf(EmailTakenException.class);
    assertThatThrownBy(() -> users.createUser("admin@example.com", "password123", UserRank.MEMBER, UserGroup.USER, true)).isInstanceOf(EmailTakenException.class);*/
  }

  // TODO How do you test confirmation tokens?

  @Test
  void update_givenValidParameters_thenSuccess() {
    /*User user = users.update(restrictedUserId, new UpdateUserParameters()
        .email("unrestricted@example.com")
        .password("newpassword")
        .group(UserGroup.USER)
        .rank(UserRank.CONTRIBUTOR)
        .verified(true));
    assertThat(user.getEmail()).isEqualTo("unrestricted@example.com");
    assertThat(user.getPassword()).isNotEqualTo("newpassword");
    assertThat(user.getGroup()).isEqualTo(UserGroup.USER);
    assertThat(user.getRank()).isEqualTo(UserRank.CONTRIBUTOR);
    assertThat(user.isVerified()).isTrue();*/
  }

  @Test
  void update_givenTakenEmail_thenThrow() {
    /*assertThatThrownBy(() -> users.update(restrictedUserId, new UpdateUserParameters()
        .email("verified@example.com")
        .password("newpassword")
        .group(UserGroup.USER)
        .rank(UserRank.CONTRIBUTOR)
        .verified(true))).isInstanceOf(EmailTakenException.class);*/
  }

  @Test
  void search_givenNoArguments_thenSuccess() {
    User u1Expect = MockUser.restrictedUser();
    u1Expect.setId(restrictedUserId);
    User u2Expect = MockUser.unverifiedMember();
    u2Expect.setId(unverifiedMemberId);
    User u3Expect = MockUser.verifiedMember();
    u3Expect.setId(verifiedMemberId);
    User u4Expect = MockUser.contributor();
    u4Expect.setId(contributorId);
    User u5Expect = MockUser.admin();
    u5Expect.setId(adminId);
    
    Page<User> page = users.search(null, PageRequest.of(0, 20));
    assertThat(page).isNotEmpty();
    assertThat(page).contains(u1Expect, u2Expect, u3Expect, u4Expect, u5Expect);
  }

  @Test
  void getCount_thenSuccess() {
    assertThat(users.getCount()).isEqualTo(5);
    users.delete(List.of(restrictedUserId));
    assertThat(users.getCount()).isEqualTo(4);
    users.delete(List.of(unverifiedMemberId));
    assertThat(users.getCount()).isEqualTo(3);
    users.delete(List.of(verifiedMemberId));
    assertThat(users.getCount()).isEqualTo(2);
    users.delete(List.of(contributorId));
    assertThat(users.getCount()).isEqualTo(1);
    users.delete(List.of(adminId));
    assertThat(users.getCount()).isEqualTo(0);
    users.registerNewUser("available@example.com", "password123");
    assertThat(users.getCount()).isEqualTo(1);
  }

  @Test
  void getUnverifiedCount_thenSuccess() {
    assertThat(users.getUnverifiedCount()).isEqualTo(1);
    users.delete(List.of(adminId));
    assertThat(users.getUnverifiedCount()).isEqualTo(1);
    users.delete(List.of(unverifiedMemberId));
    assertThat(users.getUnverifiedCount()).isEqualTo(0);
    /*users.createUser("anotherunverified@example.com", "password123", UserRank.MEMBER, UserGroup.USER, false);
    assertThat(users.getUnverifiedCount()).isEqualTo(1);*/
  }

}
