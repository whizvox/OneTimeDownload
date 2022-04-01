package me.whizvox.otdl.storage.local;

import me.whizvox.otdl.storage.InputFile;
import me.whizvox.otdl.storage.StorageException;
import me.whizvox.otdl.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@ConditionalOnProperty(
    value = "otdl.storage.module",
    havingValue = "local"
)
public class LocalStorageService implements StorageService {

  private static final Logger LOG = LoggerFactory.getLogger(LocalStorageService.class);

  private final LocalStorageConfiguration config;
  private final Path root;

  @Autowired
  public LocalStorageService(LocalStorageConfiguration config) {
    LOG.info("Selected storage system: local file system");
    this.config = config;
    root = Paths.get(config.getLocation()).normalize().toAbsolutePath();

    try {
      LOG.debug("Attempting to mkdirs to {}", root);
      Files.createDirectories(root);
    } catch (IOException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public boolean exists(String path) {
    return Files.exists(root.resolve(path));
  }

  @Override
  public void store(InputFile file, String path) {
    if (exists(path)) {
      throw new StorageException("Path already exists");
    }
    try (InputStream in = file.openStream()) {
      Files.copy(in, root.resolve(path));
    } catch (IOException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public InputStream openStream(String path) {
    try {
      return Files.newInputStream(root.resolve(path));
    } catch (IOException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public void delete(String path) {
    try {
      Files.deleteIfExists(root.resolve(path));
    } catch (IOException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public void delete(Iterable<String> paths) {
    try {
      for (String path : paths) {
        Files.deleteIfExists(root.resolve(path));
      }
    } catch (IOException e) {
      throw new StorageException(e);
    }
  }

}
