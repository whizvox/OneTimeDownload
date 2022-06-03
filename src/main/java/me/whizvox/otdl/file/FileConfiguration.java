package me.whizvox.otdl.file;

import lombok.Getter;
import lombok.Setter;
import me.whizvox.otdl.user.User;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otdl.file")
@Getter @Setter
public class FileConfiguration {

  private String tempDirectoryLocation = "temp";

  private int maxLifespanGuest = 30;

  private int maxLifespanMember = 720;

  private int maxLifespanContributor = 14400;

  private long maxFileSizeGuest = 50000000;

  private long maxFileSizeMember = 350000000;

  private long maxFileSizeContributor = 2000000000;

  private long lifespanAfterAccess = 15;

  public int getMaxLifespan(User user) {
    if (user == null) {
      return maxLifespanGuest;
    }
    return switch (user.getRole()) {
      case RESTRICTED -> 0;
      case GUEST -> maxLifespanGuest;
      case MEMBER -> maxLifespanMember;
      case CONTRIBUTOR -> maxLifespanContributor;
      case ADMIN -> Integer.MAX_VALUE;
    };
  }

  public long getMaxFileSize(User user) {
    if (user == null) {
      return maxFileSizeGuest;
    }
    return switch (user.getRole()) {
      case RESTRICTED -> 0;
      case GUEST -> maxFileSizeGuest;
      case MEMBER -> maxFileSizeMember;
      case CONTRIBUTOR -> maxFileSizeContributor;
      case ADMIN -> Long.MAX_VALUE;
    };
  }

}
