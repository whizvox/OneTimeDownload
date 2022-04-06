package me.whizvox.otdl.user;

import me.whizvox.otdl.exception.EmailTakenException;
import me.whizvox.otdl.exception.InvalidPasswordException;
import me.whizvox.otdl.exception.TokenDoesNotExistException;
import me.whizvox.otdl.util.ApiResponse;
import me.whizvox.otdl.util.PagedResponseData;
import me.whizvox.otdl.util.RequestUtils;
import me.whizvox.otdl.util.params.UpdateUserParameters;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

  @GetMapping("available")
  public ResponseEntity<Object> checkIfAvailable(@RequestParam String email) {
    return ApiResponse.ok(users.isEmailAvailable(email));
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

  @PreAuthorize("@authorizationService.hasPermission(principal, 'ADMIN')")
  @PostMapping
  public ResponseEntity<Object> create(@RequestParam String email,
                                       @RequestParam String password,
                                       @RequestParam UserRank rank,
                                       @RequestParam UserGroup group,
                                       @RequestParam boolean enabled) {
    try {
      return ApiResponse.ok(new PublicUserDetails(users.createUser(email, password, rank, group, enabled)));
    } catch (EmailTakenException e) {
      return ApiResponse.badRequest("Email already taken");
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

  @PutMapping("{id}")
  @PreAuthorize("@authorizationService.canAccessUserDetails(principal, #id)")
  public ResponseEntity<Object> update(
      @PathVariable Long id,
      @RequestParam MultiValueMap<String, String> params) {
    UpdateUserParameters newParams = new UpdateUserParameters();
    newParams.setAll(params);
    users.update(id, newParams);
    return ApiResponse.ok();
  }

  @PreAuthorize("@authorizationService.hasPermission(principal, 'ADMIN')")
  @GetMapping("search")
  public ResponseEntity<Object> search(
      @And(value = {
          @Spec(params = "id", path = "id", spec = Equal.class),
          @Spec(params = "email", path = "email", spec = LikeIgnoreCase.class),
          @Spec(params = "rank", path = "rank", spec = Equal.class),
          @Spec(params = "group", path = "group", spec = Equal.class),
          @Spec(params = "enabled", path = "enabled", spec = Equal.class)
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
  public ResponseEntity<Object> delete(@RequestParam(required = false) Long[] ids) {
    if (ids != null && ids.length > 0) {
      users.delete(List.of(ids));
    }
    return ApiResponse.ok();
  }

}
