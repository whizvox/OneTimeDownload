package me.whizvox.otdl.page;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otdl.page")
@Getter @Setter
public class PageConfiguration {

  private String titleFormat = "%1$s | One-Time Download";

}
