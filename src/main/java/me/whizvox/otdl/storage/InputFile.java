package me.whizvox.otdl.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public interface InputFile {

  InputStream openStream() throws IOException;

  long getSize();

  record MultipartInputFile(MultipartFile file) implements InputFile {
    @Override
    public InputStream openStream() throws IOException {
      return file.getInputStream();
    }
    @Override
    public long getSize() {
      return file.getSize();
    }
  }

  record LocalInputFile(Path path) implements InputFile {
    @Override
    public InputStream openStream() throws IOException {
      return Files.newInputStream(path);
    }
    @Override
    public long getSize() {
      try {
        return Files.size(path);
      } catch (IOException ignored) {
      }
      return 0;
    }
  }

  record InputStreamFile(InputStream in, long size) implements InputFile {
    @Override
    public InputStream openStream() throws IOException {
      return in;
    }
    @Override
    public long getSize() {
      return size;
    }
  }

  static InputFile multipart(MultipartFile file) {
    return new MultipartInputFile(file);
  }

  static InputFile local(Path path) {
    return new LocalInputFile(path);
  }

  static InputFile inputStream(InputStream in, long size) {
    return new InputStreamFile(in, size);
  }

}
