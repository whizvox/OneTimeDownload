package me.whizvox.otdl.file;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otdl.file")
public class FileConfiguration {

  @Getter @Setter
  private String uploadedFilesDirectory = "files";

  @Getter @Setter
  private int maxLifespanAnonymous = 60;

  @Getter @Setter
  private int maxLifespanMember = 1440;

  @Getter @Setter
  private int maxFileSizeAnonymous = 10000000;

  @Getter @Setter
  private long maxFileSizeMember = 250000000;

  @Getter @Setter
  private long lifespanAfterAccess = 15;

}
