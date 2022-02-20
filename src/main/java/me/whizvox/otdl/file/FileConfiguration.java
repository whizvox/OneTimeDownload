package me.whizvox.otdl.file;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otdl.file")
public class FileConfiguration {

  @Getter @Setter
  private String uploadedFilesDirectory = "files";

}
