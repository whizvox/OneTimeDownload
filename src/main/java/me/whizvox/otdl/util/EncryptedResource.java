package me.whizvox.otdl.util;

import org.springframework.core.io.AbstractResource;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class EncryptedResource extends AbstractResource {

  private final Path inputFilePath;
  private final Cipher cipher;

  public EncryptedResource(Path inputFilePath, Cipher cipher) {
    this.inputFilePath = inputFilePath;
    this.cipher = cipher;
  }

  @Override
  public String getDescription() {
    return "path=%s, cipher=%s".formatted(inputFilePath.getFileName().toString(), cipher.toString());
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new CipherInputStream(Files.newInputStream(inputFilePath), cipher);
  }

}
