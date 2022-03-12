package me.whizvox.otdl.storage.s3;

import me.whizvox.otdl.storage.InputFile;
import me.whizvox.otdl.storage.StorageException;
import me.whizvox.otdl.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;

@Service
@ConditionalOnProperty(
    value = "otdl.storage.module",
    havingValue = "s3",
    matchIfMissing = true
)
public class S3StorageService implements StorageService {

  private static final Logger LOG = LoggerFactory.getLogger(S3StorageService.class);

  private final S3ConfigurationProperties config;
  private final S3Client s3;

  private final String bucketName;

  @Autowired
  public S3StorageService(S3ConfigurationProperties config, S3Client s3) {
    LOG.info("Selected storage service: AWS S3");
    this.config = config;
    this.s3 = s3;

    bucketName = config.getBucketName();

    try {
      s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
      LOG.debug("Bucket already exists. No further action");
    } catch (NoSuchBucketException ignored) {
      LOG.info("Bucket does not yet exist. Creating...");
      s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    }
  }

  @Override
  public boolean exists(String path) {
    try {
      s3.headObject(HeadObjectRequest.builder().bucket(bucketName).key(path).build());
      return true;
    } catch (NoSuchKeyException e) {
      return false;
    }
  }

  @Override
  public void store(InputFile file, String path) {
    try (InputStream in = file.openStream()) {
      s3.putObject(PutObjectRequest.builder().bucket(bucketName).key(path).build(), RequestBody.fromInputStream(in, file.getSize()));
    } catch (IOException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public InputStream openStream(String path) {
    return s3.getObject(GetObjectRequest.builder().bucket(bucketName).key(path).build(), ResponseTransformer.toInputStream());
  }

  @Override
  public void delete(String path) {
    s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(path).build());
  }

}
