package me.whizvox.otdl.util;

import me.whizvox.otdl.page.Page;
import me.whizvox.otdl.page.PageConfiguration;
import me.whizvox.otdl.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PageUtils {

  private PageConfiguration config;

  @Autowired
  public PageUtils(PageConfiguration config) {
    this.config = config;
  }

  public Page createStandardPage(String title, String href, User user) {
    Page page = new Page();
    page.setTitle(config.getTitleFormat().formatted(title));
    page.setHref(href);
    page.setUser(StringUtils.getObscuredEmail(user.getUsername()));
    return page;
  }

}
