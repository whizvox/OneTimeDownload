package me.whizvox.otdl.util;

import me.whizvox.otdl.page.Page;
import me.whizvox.otdl.user.User;

public class PageUtils {

  public static Page createStandardPage(String title, String href, User user) {
    Page page = new Page();
    page.setTitle(title);
    page.setHref(href);
    page.setUser(StringUtils.getObscuredEmail(user.getUsername()));
    return page;
  }

}
