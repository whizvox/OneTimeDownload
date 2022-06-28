package me.whizvox.otdl.util;

import me.whizvox.otdl.page.Page;
import me.whizvox.otdl.page.PageConfiguration;
import me.whizvox.otdl.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class PageUtils {

  private PageConfiguration config;

  @Autowired
  public PageUtils(PageConfiguration config) {
    this.config = config;
  }

  public Page createStandardPage(String title, String href) {
    Page page = new Page();
    page.setTitle(config.getTitleFormat().formatted(title));
    page.setHref(href);
    return page;
  }

  public ModelAndView noUser(String template, String title, String href) {
    return new ModelAndView(template)
        .addObject("page", createStandardPage(title, href));
  }

  public ModelAndView withUser(String view, String title, String href, User user) {
    return noUser(view, title, href)
        .addObject("user", user);
  }

  public ModelAndView redirect(String href) {
    return new ModelAndView("redirect:" + href);
  }

  public ModelAndView redirectToIndex() {
    return redirect("/");
  }

}
