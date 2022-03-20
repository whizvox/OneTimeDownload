package me.whizvox.otdl.page;

import me.whizvox.otdl.exception.TokenDoesNotExistException;
import me.whizvox.otdl.file.FileInfo;
import me.whizvox.otdl.file.FileService;
import me.whizvox.otdl.user.User;
import me.whizvox.otdl.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@RestController
public class PageController {

  private final PageConfiguration config;
  private final FileService files;
  private final UserService users;

  @Autowired
  public PageController(PageConfiguration config, FileService files, UserService users) {
    this.config = config;
    this.files = files;
    this.users = users;
  }

  private Page createStandardPage(String title, String href) {
    Page page = new Page();
    page.setTitle(String.format(config.getTitleFormat(), title));
    page.setHref(href);
    return page;
  }

  @GetMapping({"/", "index", "home"})
  public ModelAndView index(@AuthenticationPrincipal User user, HttpServletRequest req) {
    req.isUserInRole("ADMIN");
    return new ModelAndView("upload")
        .addObject("page", createStandardPage("Home", "/"))
        .addObject("user", user);
  }

  @GetMapping("/view/{fileId}")
  public ModelAndView view(@PathVariable String fileId, @AuthenticationPrincipal User user) {
    FileInfo info = files.getInfo(fileId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    return new ModelAndView("view")
        .addObject("page", createStandardPage("View file", "/view"))
        .addObject("file", new ViewPageFileInfo(info))
        .addObject("user", user);
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
    if (user == null || !user.isLoggedIn()) {
      return new ModelAndView("login")
          .addObject("page", createStandardPage("Login", "/login"));
    }
    return new ModelAndView("redirect:/");
  }

  @GetMapping("/register")
  public ModelAndView register(@AuthenticationPrincipal User user) {
    if (user == null || !user.isLoggedIn()) {
      return new ModelAndView("register")
          .addObject("page", createStandardPage("Register", "/register"));
    }
    return new ModelAndView("redirect:/");
  }

  @GetMapping("/debug")
  public ModelAndView debug(@AuthenticationPrincipal User user) {
    return new ModelAndView("debug")
        .addObject("page", createStandardPage("Debug", "/debug"))
        .addObject("user", user);
  }

  @GetMapping("/confirm/{token}")
  public ModelAndView confirm(@PathVariable String token) {
    try {
      users.confirmUser(token);
    } catch (TokenDoesNotExistException ignored) {}
    return new ModelAndView("redirect:/login?confirmed");
  }

  @GetMapping("/need-confirm")
  public ModelAndView needConfirm(@AuthenticationPrincipal User user) {
    if (user != null && user.isLoggedIn()) {
      return new ModelAndView("redirect:/");
    }
    return new ModelAndView("need-confirm")
        .addObject("page", createStandardPage("Need account confirmation", "/need-confirm"));
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
