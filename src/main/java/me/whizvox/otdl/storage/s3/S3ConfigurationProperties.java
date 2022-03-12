package me.whizvox.otdl.storage.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otdl.storage.s3")
@Getter @Setter
public class S3ConfigurationProperties {

  private String bucketName;

}
