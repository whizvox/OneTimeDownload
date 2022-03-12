package me.whizvox.otdl.storage.local;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otdl.storage.local")
@Getter @Setter
public class LocalStorageConfiguration {

  private String location = "files";

}
