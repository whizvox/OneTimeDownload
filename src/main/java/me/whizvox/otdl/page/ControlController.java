package me.whizvox.otdl.page;

import me.whizvox.otdl.user.User;
import me.whizvox.otdl.util.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/control")
public class ControlController {

  private final PageUtils utils;

  @Autowired
  public ControlController(PageUtils utils) {
    this.utils = utils;
  }

  @GetMapping
  public ModelAndView index(@AuthenticationPrincipal User user) {
    return new ModelAndView("control")
        .addObject("page", utils.createStandardPage("Control panel", "/control", user));
  }

  @GetMapping("files")
  public ModelAndView files(@AuthenticationPrincipal User user) {
    return new ModelAndView("control_files")
        .addObject("page", utils.createStandardPage("Control files", "/control/files", user));
  }

  @GetMapping("users")
  public ModelAndView users(@AuthenticationPrincipal User user) {
    return new ModelAndView("control_users")
        .addObject("page", utils.createStandardPage("Control users", "/control/users", user))
        .addObject("user", user);
  }

  @GetMapping("users/create")
  public ModelAndView createUser(@AuthenticationPrincipal User user) {
    return new ModelAndView("control_create_user")
        .addObject("page", utils.createStandardPage("Create new user", "/control/users/create", user))
        .addObject("user", user);
  }

}
