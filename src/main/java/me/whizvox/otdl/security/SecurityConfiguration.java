package me.whizvox.otdl.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otdl.security")
public class SecurityConfiguration {

  @Getter @Setter
  private int keyLength = 150;

  @Getter @Setter
  private int saltSize = 8;

  @Getter @Setter
  private String passwordHashAlgorithm = "PBKDF2WithHmacSHA1";

  @Getter @Setter
  private int iterationCount = 65535;

  @Getter @Setter
  private String cipherAlgorithm = "Blowfish";

  @Getter @Setter
  private String fullCipherTransformation = "Blowfish";

}
