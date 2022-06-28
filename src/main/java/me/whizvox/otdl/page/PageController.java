package me.whizvox.otdl.page;

import me.whizvox.otdl.exception.TokenDoesNotExistException;
import me.whizvox.otdl.file.FileService;
import me.whizvox.otdl.user.User;
import me.whizvox.otdl.user.UserConfigurationProperties;
import me.whizvox.otdl.user.UserService;
import me.whizvox.otdl.util.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class PageController {

  private final PageUtils utils;
  private final FileService files;
  private final UserService users;
  private final UserConfigurationProperties props;

  @Autowired
  public PageController(PageUtils utils,
                        FileService files,
                        UserService users,
                        UserConfigurationProperties props) {
    this.utils = utils;
    this.files = files;
    this.users = users;
    this.props = props;
  }

  private boolean isLoggedIn(@Nullable User user) {
    return user != null && !user.isGuest();
  }

  @GetMapping("/")
  public ModelAndView index(@AuthenticationPrincipal User user) {
    return utils.withUser("upload", "Home", "/", user);
  }

  @GetMapping({"/index", "/home"})
  public ModelAndView indexAliases() {
    return utils.redirectToIndex();
  }

  @GetMapping("/view/{fileId}")
  public ModelAndView view(@PathVariable String fileId, @AuthenticationPrincipal User user) {
    ModelAndView mav = utils.withUser("view", "View file", "/view", user);
    files.getInfo(fileId).ifPresent(file -> mav.addObject("file", new ViewPageFileInfo(file)));
    return mav;
  }

  @GetMapping("/download/{fileId}")
  public ModelAndView download(@PathVariable String fileId, @AuthenticationPrincipal User user) {
    return utils.withUser("download", "Download file", "/download", user)
        .addObject("fileId", fileId);
  }

  @GetMapping("/login")
  public ModelAndView login(@AuthenticationPrincipal User user) {
    if (isLoggedIn(user)) {
      return utils.redirectToIndex();
    }
    return utils.noUser("login", "Login", "/login");
  }

  @GetMapping("/register")
  public ModelAndView register(@AuthenticationPrincipal User user) {
    if (isLoggedIn(user)) {
      return utils.redirectToIndex();
    }
    return utils.noUser("register", "Register", "/register")
        .addObject("passwordRegex", props.getPasswordRequirementRegex())
        .addObject("passwordRequirements", props.getPasswordRequirementDescription());
  }

  @GetMapping("/verify/{token}")
  public ModelAndView verify(@PathVariable String token,
                             @AuthenticationPrincipal User user) {
    try {
      users.confirmUser(token);
      // successful
      if (user != null) {
        user.setVerified(true);
      }
    } catch (TokenDoesNotExistException ignored) {
      return utils.redirect("/?expired");
    }
    if (isLoggedIn(user)) {
      return utils.redirect("/?verified");
    }
    return utils.redirect("login?verified");
  }

  @GetMapping("/need-verify")
  public ModelAndView needVerify(@AuthenticationPrincipal User user) {
    if (isLoggedIn(user)) {
      return utils.redirectToIndex();
    }
    if (users.getShouldVerifyEmail()) {
      return utils.withUser("need_verify", "Need account verification", "/need-verify", user);
    }
    return utils.redirect("/login?created");
  }

  @GetMapping("/profile")
  public ModelAndView viewProfile(@AuthenticationPrincipal User user) {
    if (!isLoggedIn(user)) {
      return utils.redirectToIndex();
    }
    return utils.withUser("profile", "View profile", "/profile", user)
        .addObject("passwordRegex", props.getPasswordRequirementRegex())
        .addObject("passwordRequirements", props.getPasswordRequirementDescription());
  }

  @GetMapping("/reset/{token}")
  public ModelAndView resetPassword(@PathVariable String token,
                                    @AuthenticationPrincipal User user) {
    return utils.withUser("reset_password", "Reset password", "/reset", user)
        .addObject("passwordRegex", props.getPasswordRequirementRegex())
        .addObject("passwordRequirements", props.getPasswordRequirementDescription())
        .addObject("token", token);
  }

  @GetMapping("/forgot-password")
  public ModelAndView forgotPassword(@AuthenticationPrincipal User user) {
    if (isLoggedIn(user)) {
      return utils.redirectToIndex();
    }
    return utils.noUser("forgot_password", "Forgot password", "/forgot-password");
  }

  @GetMapping("contact")
  public ModelAndView contact(@AuthenticationPrincipal User user) {
    return utils.withUser("contact", "Contact", "/contact", user);
  }

  @GetMapping("about")
  public ModelAndView about(@AuthenticationPrincipal User user) {
    return utils.withUser("about", "About", "/about", user);
  }

}
