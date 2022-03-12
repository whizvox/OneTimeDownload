package me.whizvox.otdl;

import me.whizvox.otdl.file.FileConfiguration;
import me.whizvox.otdl.page.PageConfiguration;
import me.whizvox.otdl.security.SecurityConfiguration;
import me.whizvox.otdl.storage.local.LocalStorageConfiguration;
import me.whizvox.otdl.storage.s3.S3ConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
    FileConfiguration.class,
    SecurityConfiguration.class,
    PageConfiguration.class,
    LocalStorageConfiguration.class,
    S3ConfigurationProperties.class
})
@PropertySources({
    @PropertySource("classpath:application.properties"),
    @PropertySource(value = "file:application.properties", ignoreResourceNotFound = true)
})
public class OneTimeDownloadApplication {

  public static void main(String[] args) {
    SpringApplication.run(OneTimeDownloadApplication.class, args);
  }

}
