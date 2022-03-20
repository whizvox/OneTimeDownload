package me.whizvox.otdl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan("me.whizvox.otdl")
@PropertySources({
    @PropertySource("classpath:application.properties"),
    @PropertySource(value = "file:application.properties", ignoreResourceNotFound = true)
})
public class OneTimeDownloadApplication {

  public static void main(String[] args) {
    SpringApplication.run(OneTimeDownloadApplication.class, args);
  }

}
