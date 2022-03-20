package me.whizvox.otdl.user;

import me.whizvox.otdl.exception.EmailTakenException;
import me.whizvox.otdl.exception.InvalidPasswordException;
import me.whizvox.otdl.exception.TokenDoesNotExistException;
import me.whizvox.otdl.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UserController {

  private final UserService users;
  private final UserConfigurationProperties config;

  @Autowired
  public UserController(UserService users, UserConfigurationProperties config) {
    this.users = users;
    this.config = config;
  }

  @PreAuthorize("@authorizationService.canAccessUserDetails(principal, #id)")
  @GetMapping("{id}")
  public ResponseEntity<Object> getUserDetails(@PathVariable Long id) {
    return users.getUserDetails(id).map(ApiResponse::ok).orElseGet(() -> ApiResponse.notFound("" + id));
  }

  @PostMapping("register")
  public ResponseEntity<Object> register(@RequestParam(required = false) String email, @RequestParam(required = false) String password) {
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

  @PostMapping("confirm/{token}")
  public ResponseEntity<Object> confirmUser(@PathVariable(required = false) String token) {
    if (token == null) {
      return ApiResponse.badRequest("Token must be defined");
    }
    try {
      users.confirmUser(token);
      return ApiResponse.ok();
    } catch (TokenDoesNotExistException e) {
      return ApiResponse.notFound(token);
    }
  }

}
