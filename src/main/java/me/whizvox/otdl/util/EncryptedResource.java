package me.whizvox.otdl.util;

import org.springframework.core.io.InputStreamResource;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EncryptedResource extends InputStreamResource {

  private final Cipher cipher;
  private final String originalFileName;
  private final long contentLength;

  public EncryptedResource(InputStream in, Cipher cipher, String originalFileName, long contentLength) {
    super(in);
    this.cipher = cipher;
    this.originalFileName = originalFileName;
    this.contentLength = contentLength;
  }

  @Override
  public String getDescription() {
    return "Encrypted resource: cipher=%s".formatted(cipher.toString());
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new CipherInputStream(super.getInputStream(), cipher);
  }

  @Override
  public String getFilename() {
    return originalFileName;
  }

  @Override
  public long contentLength() throws IOException {
    return contentLength;
  }

}
