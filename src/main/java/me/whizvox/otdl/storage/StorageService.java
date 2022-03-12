package me.whizvox.otdl.storage;

import java.io.InputStream;

public interface StorageService {

  boolean exists(String path);

  void store(InputFile file, String path);

  InputStream openStream(String path);

  void delete(String path);

}
