package me.whizvox.otdl.user;

import me.whizvox.otdl.exception.*;
import me.whizvox.otdl.util.ApiResponse;
import me.whizvox.otdl.util.PagedResponseData;
import me.whizvox.otdl.util.RequestUtils;
import me.whizvox.otdl.util.params.UpdateUserParameters;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LessThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("users")
public class UserController {

  private final UserService users;
  private final UserConfigurationProperties config;

  @Autowired
  public UserController(UserService users,
                        UserConfigurationProperties config) {
    this.users = users;
    this.config = config;
  }

  @PreAuthorize("@authorizationService.canAccessUserDetails(principal, #id)")
  @GetMapping("{id}")
  public ResponseEntity<Object> getUserDetails(@PathVariable UUID id) {
    return users.getUserDetails(id).map(ApiResponse::ok).orElseGet(() -> ApiResponse.notFound(id.toString(), "user"));
  }

  @GetMapping("available")
  public ResponseEntity<Object> checkIfAvailable(@RequestParam String email) {
    return ApiResponse.ok(users.isEmailAvailable(email));
  }

  @PostMapping("register")
  public ResponseEntity<Object> register(@RequestParam(required = false) String email,
                                         @RequestParam(required = false) String password) {
    if (email == null) {
      return ApiResponse.badRequest("Email must be defined");
    }
    if (password == null) {
      return ApiResponse.badRequest("Password must be defined");
    }
    try {
      return ApiResponse.ok(new PublicUserDetails(users.registerNewUser(email, password)));
    } catch (InvalidPasswordException e) {
      return ApiResponse.badRequest("Invalid password, " + config.getPasswordRequirementDescription());
    } catch (EmailTakenException e) {
      return ApiResponse.badRequest("Email taken");
    }
  }

  @PreAuthorize("@authorizationService.hasPermission(principal, 'ADMIN')")
  @PostMapping
  public ResponseEntity<Object> create(@RequestParam String email,
                                       @RequestParam String password,
                                       @RequestParam UserRole role,
                                       @RequestParam boolean enabled) {
    try {
      return ApiResponse.ok(new PublicUserDetails(users.createUser(email, password, role, enabled)));
    } catch (EmailTakenException e) {
      return ApiResponse.badRequest("Email already taken");
    }
  }

  @PostMapping("verify/{token}")
  public ResponseEntity<Object> verifyUserEmail(@PathVariable(required = false) String token) {
    if (token == null) {
      return ApiResponse.badRequest("Token must be defined");
    }
    try {
      users.confirmUser(token);
      return ApiResponse.ok();
    } catch (TokenDoesNotExistException e) {
      return ApiResponse.notFound(token, "verification token");
    }
  }

  @PostMapping("send-verification-email")
  public ResponseEntity<Object> sendVerificationEmail(@AuthenticationPrincipal User user) {
    if (user == null || user.isGuest()) {
      return ApiResponse.unauthorized();
    }
    users.sendVerificationEmail(user);
    return ApiResponse.ok();
  }

  @PutMapping("{id}")
  @PreAuthorize("@authorizationService.canAccessUserDetails(principal, #id)")
  public ResponseEntity<Object> update(
      @PathVariable UUID id,
      @RequestParam MultiValueMap<String, String> params) {
    UpdateUserParameters newParams = new UpdateUserParameters();
    newParams.setAll(params);
    users.update(id, newParams);
    return ApiResponse.ok();
  }

  @PutMapping("self/email")
  public ResponseEntity<Object> updateEmail(
      @RequestParam String email,
      @RequestParam CharSequence password,
      @AuthenticationPrincipal User user) {
    try {
      boolean changed = users.updateEmail(user, email, password, true);
      return ApiResponse.ok(changed);
    } catch (UnknownIdException e) {
      return ApiResponse.badRequest("Unknown user: " + user.getId());
    } catch (WrongPasswordException e) {
      return ApiResponse.forbidden("Wrong password");
    } catch (EmailTakenException e) {
      return ApiResponse.badRequest("Email taken");
    }
  }

  @PutMapping("self/password")
  public ResponseEntity<Object> updatePassword(
      @RequestParam CharSequence newPassword,
      @RequestParam CharSequence password,
      @AuthenticationPrincipal User user) {
    try {
      boolean changed = users.updatePassword(user, newPassword, password);
      return ApiResponse.ok(changed);
    } catch (UnknownIdException e) {
      return ApiResponse.badRequest("Unknown user: " + user.getId());
    } catch (WrongPasswordException e) {
      return ApiResponse.forbidden("Wrong password");
    } catch (InvalidPasswordException e) {
      return ApiResponse.badRequest("Invalid password");
    }
  }

  @DeleteMapping("self")
  public ResponseEntity<Object> deactivate(
      @RequestParam CharSequence password,
      @AuthenticationPrincipal User user) {
    try {
      boolean deactivated = users.deactivate(user.getId(), password);
      return ApiResponse.ok(deactivated);
    } catch (WrongPasswordException e) {
      return ApiResponse.badRequest("Wrong password");
    }
  }

  @PostMapping("reset")
  public ResponseEntity<Object> requestPasswordReset(@RequestParam String email) {
    boolean sent = users.requestPasswordReset(email);
    return ApiResponse.ok(sent);
  }

  @PutMapping("reset")
  public ResponseEntity<Object> resetPassword(
      @RequestParam UUID token,
      @RequestParam CharSequence password) {
    try {
      if (users.resetPassword(token, password)) {
        return ApiResponse.ok();
      } else {
        return ApiResponse.notFound(token.toString(), "password reset token");
      }
    } catch (InvalidPasswordException e) {
      return ApiResponse.badRequest("Invalid password");
    }
  }

  @PreAuthorize("@authorizationService.hasPermission(principal, 'ADMIN')")
  @GetMapping("search")
  public ResponseEntity<Object> search(
      @And(value = {
          @Spec(params = "id", path = "id", spec = Equal.class),
          @Spec(params = "email", path = "email", spec = LikeIgnoreCase.class),
          @Spec(params = "role", path = "role", spec = Equal.class),
          @Spec(params = "verified", path = "verified", spec = Equal.class),
          @Spec(params = "createdAfter", path = "created", spec = GreaterThanOrEqual.class),
          @Spec(params = "createdBefore", path = "created", spec = LessThanOrEqual.class)
      }) Specification<User> spec,
      Pageable pageable) {
    return ApiResponse.ok(
        new PagedResponseData<>(
            users.search(spec, RequestUtils.pageableWithDefaultSort(pageable, true, "id"))
                .map(PublicUserDetails::new)
        )
    );
  }

  @PreAuthorize("@authorizationService.hasPermission(principal, 'ADMIN')")
  @PostMapping("delete")
  public ResponseEntity<Object> delete(@RequestParam(required = false) UUID[] ids) {
    if (ids != null && ids.length > 0) {
      users.delete(List.of(ids));
    }
    return ApiResponse.ok();
  }

}
