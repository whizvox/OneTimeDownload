package me.whizvox.otdl.page;

import me.whizvox.otdl.exception.TokenDoesNotExistException;
import me.whizvox.otdl.file.FileService;
import me.whizvox.otdl.user.User;
import me.whizvox.otdl.user.UserConfigurationProperties;
import me.whizvox.otdl.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class PageController {

  private final PageConfiguration config;
  private final FileService files;
  private final UserService users;
  private final UserConfigurationProperties props;

  @Autowired
  public PageController(PageConfiguration config,
                        FileService files,
                        UserService users,
                        UserConfigurationProperties props) {
    this.config = config;
    this.files = files;
    this.users = users;
    this.props = props;
  }

  private Page createStandardPage(String title, String href) {
    Page page = new Page();
    page.setTitle(String.format(config.getTitleFormat(), title));
    page.setHref(href);
    return page;
  }

  @GetMapping("/")
  public ModelAndView index(@AuthenticationPrincipal User user) {
    return new ModelAndView("upload")
        .addObject("page", createStandardPage("Home", "/"))
        .addObject("user", user);
  }

  @GetMapping({"/index", "/home"})
  public ModelAndView indexAliases() {
    return new ModelAndView("redirect:/");
  }

  @GetMapping("/view/{fileId}")
  public ModelAndView view(@PathVariable String fileId, @AuthenticationPrincipal User user) {
    ModelAndView mav = new ModelAndView("view")
        .addObject("page", createStandardPage("View file", "/view"))
        .addObject("user", user);
    files.getInfo(fileId).ifPresent(file -> mav.addObject("file", new ViewPageFileInfo(file)));
    return mav;
  }

  @GetMapping("/download/{fileId}")
  public ModelAndView download(@PathVariable String fileId, @AuthenticationPrincipal User user) {
    return new ModelAndView("download")
        .addObject("page", createStandardPage("Download file", "/download"))
        .addObject("fileId", fileId)
        .addObject("user", user);
  }

  @GetMapping("/login")
  public ModelAndView login(@AuthenticationPrincipal User user) {
    if (user == null || !user.isGuest()) {
      return new ModelAndView("login")
          .addObject("page", createStandardPage("Login", "/login"));
    }
    return new ModelAndView("redirect:/");
  }

  @GetMapping("/register")
  public ModelAndView register(@AuthenticationPrincipal User user) {
    if (user == null || !user.isGuest()) {
      return new ModelAndView("register")
          .addObject("page", createStandardPage("Register", "/register"))
          .addObject("passwordRegex", props.getPasswordRequirementRegex())
          .addObject("passwordRequirements", props.getPasswordRequirementDescription());
    }
    return new ModelAndView("redirect:/");
  }

  @GetMapping("/debug")
  public ModelAndView debug(@AuthenticationPrincipal User user) {
    return new ModelAndView("debug")
        .addObject("page", createStandardPage("Debug", "/debug"))
        .addObject("user", user);
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
      return new ModelAndView("redirect:/?expired");
    }
    if (user == null || user.isGuest()) {
      return new ModelAndView("redirect:/login?verified");
    }
    return new ModelAndView("redirect:/?verified");
  }

  @GetMapping("/need-verify")
  public ModelAndView needConfirm(@AuthenticationPrincipal User user) {
    if (user != null && user.isGuest()) {
      return new ModelAndView("redirect:/");
    }
    if (users.getShouldVerifyEmail()) {
      return new ModelAndView("need_verify")
          .addObject("page", createStandardPage("Need account confirmation", "/need-verify"));
    }
    return new ModelAndView("redirect:/login?created");
  }

  @GetMapping("/profile")
  public ModelAndView viewProfile(@AuthenticationPrincipal User user) {
    if (user == null || user.isGuest()) {
      return new ModelAndView("redirect:/");
    }
    return new ModelAndView("profile")
        .addObject("page", createStandardPage("View profile", "/profile"))
        .addObject("passwordRegex", props.getPasswordRequirementRegex())
        .addObject("passwordRequirements", props.getPasswordRequirementDescription())
        .addObject("user", user);
  }

  @GetMapping("/reset/{token}")
  public ModelAndView resetPassword(@PathVariable String token) {
    return new ModelAndView("reset_password")
        .addObject("page", createStandardPage("Reset password", "/reset"))
        .addObject("passwordRegex", props.getPasswordRequirementRegex())
        .addObject("passwordRequirements", props.getPasswordRequirementDescription())
        .addObject("token", token);
  }

  @GetMapping("/forgot-password")
  public ModelAndView forgotPassword(@AuthenticationPrincipal User user) {
    if (user != null && !user.isGuest()) {
      return new ModelAndView("redirect:/");
    }
    return new ModelAndView("forgot_password")
        .addObject("page", createStandardPage("Forgot password", "/forgot-password"));
  }

  /*@GetMapping("contact")
  public ModelAndView contact() {
    return new ModelAndView("contact")
        .addObject("page", createStandardPage("Contact", "/contact"));
  }*/

  /*@GetMapping("about")
  public ModelAndView about() {
    return new ModelAndView("about")
        .addObject("page", createStandardPage("About", "/about"));
  }*/

}
