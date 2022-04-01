package me.whizvox.otdl.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otdl.security")
@Getter @Setter
public class SecurityConfiguration {

  private int keyLength = 150;

  private int saltSize = 8;

  private String passwordHashAlgorithm = "PBKDF2WithHmacSHA1";

  private int iterationCount = 65535;

  private String cipherAlgorithm = "Blowfish";

  private String fullCipherTransformation = "Blowfish";

  private boolean enableCsrf = true;

}
