package me.whizvox.otdl.file;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otdl.file")
@Getter @Setter
public class FileConfiguration {

  private String tempDirectoryLocation = "temp";

  private int maxLifespanAnonymous = 60;

  private int maxLifespanMember = 1440;

  private int maxFileSizeAnonymous = 10000000;

  private long maxFileSizeMember = 250000000;

  private long lifespanAfterAccess = 15;

}
