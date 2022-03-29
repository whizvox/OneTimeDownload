package me.whizvox.otdl.file;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otdl.file")
@Getter @Setter
public class FileConfiguration {

  private String tempDirectoryLocation = "temp";

  private int maxLifespanAnonymous = 30;

  private int maxLifespanMember = 720;

  private int maxLifespanContributor = 14400;

  private int maxFileSizeAnonymous = 50000000;

  private long maxFileSizeMember = 350000000;

  private long maxFileSizeContributor = 2000000000;

  private long lifespanAfterAccess = 15;

}
