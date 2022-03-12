package me.whizvox.otdl.storage.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(
    value = "otdl.storage.module",
    havingValue = "s3",
    matchIfMissing = true
)
public class S3Configuration {

  @Value("${otdl.storage.s3.credentials.access-key}")
  private String accessKey;

  @Value("${otdl.storage.s3.credentials.secret-key}")
  private String secretKey;

  @Value("${otdl.storage.s3.region.static}")
  private String staticRegion;

  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .region(Region.of(staticRegion))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build();
  }

}
