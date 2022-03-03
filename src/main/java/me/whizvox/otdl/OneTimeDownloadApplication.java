package me.whizvox.otdl;

import me.whizvox.otdl.file.FileConfiguration;
import me.whizvox.otdl.page.PageConfiguration;
import me.whizvox.otdl.security.SecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({FileConfiguration.class, SecurityConfiguration.class, PageConfiguration.class})
public class OneTimeDownloadApplication {

  public static void main(String[] args) {
    SpringApplication.run(OneTimeDownloadApplication.class, args);
  }

}
