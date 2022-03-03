package me.whizvox.otdl.page;

import me.whizvox.otdl.file.FileInfo;
import me.whizvox.otdl.file.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@RestController
public class PageController {

  private final PageConfiguration config;
  private final FileService files;

  @Autowired
  public PageController(PageConfiguration config, FileService files) {
    this.config = config;
    this.files = files;
  }

  private Page createStandardPage(String title, String href) {
    Page page = new Page();
    page.setTitle(String.format(config.getTitleFormat(), title));
    page.setHref(href);
    return page;
  }

  @GetMapping({"/", "index", "home"})
  public ModelAndView index() {
    return new ModelAndView("upload")
        .addObject("page", createStandardPage("Home", "/"));
  }

  @GetMapping("/view/{fileId}")
  public ModelAndView view(@PathVariable String fileId) {
    FileInfo info = files.getInfo(fileId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    return new ModelAndView("view")
        .addObject("page", createStandardPage("View file", "/view"))
        .addObject("file", new ViewPageFileInfo(info));
  }

  @GetMapping("/download/{fileId}")
  public ModelAndView download(@PathVariable String fileId) {
    FileInfo info = files.getInfo(fileId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    return new ModelAndView("download")
        .addObject("page", createStandardPage("Download file", "/download"))
        .addObject("file", new ViewPageFileInfo(info));
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
