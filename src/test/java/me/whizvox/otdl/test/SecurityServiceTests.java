package me.whizvox.otdl.test;

import me.whizvox.otdl.security.ComboAuthToken;
import me.whizvox.otdl.security.SecurityConfiguration;
import me.whizvox.otdl.security.SecurityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SecurityServiceTests {
  
  private final SecurityService security;
  private final SecurityConfiguration config;

  @Autowired
  public SecurityServiceTests(SecurityService security, SecurityConfiguration config) {
    this.security = security;
    this.config = config;
  }

  @Test
  public void generateSalt_thenValid() {
    byte[] salt = security.generateSalt();
    assertNotNull(salt);
    assertEquals(config.getSaltSize(), salt.length);
  }

  @Test
  public void generateSecret_givenGoodInput_thenValid() {
    assertDoesNotThrow(() -> {
      SecretKey key = security.generateSecret("password123".toCharArray(), security.generateSalt());
      assertEquals(key.getAlgorithm(), config.getPasswordHashAlgorithm());
      assertEquals(config.getKeyLength() / 8, key.getEncoded().length);
    });
  }

  @Test
  public void generateSecret_givenSameInput_thenDeterministic() {
    byte[] salt = security.generateSalt();
    byte[] key1 = security.generateSecret("password123".toCharArray(), salt).getEncoded();
    for (int i = 0; i < 20; i++) {
      byte[] key2 = security.generateSecret("password123".toCharArray(), salt).getEncoded();
      assertArrayEquals(key1, key2);
    }
  }

  @Test
  public void generateSecret_givenDiffPasswordsOrSalts_thenDiffKeys() {
    byte[] salt = security.generateSalt();
    byte[] key1 = security.generateSecret("password0".toCharArray(), salt).getEncoded();
    for (int i = 1; i <= 20; i++) {
      byte[] key2 = security.generateSecret(("password" + i).toCharArray(), salt).getEncoded();
      assertFalse(Arrays.equals(key1, key2));
    }
    for (int i = 0; i < 20; i++) {
      byte[] salt2 = security.generateSalt();
      byte[] key2 = security.generateSecret("password0".toCharArray(), salt2).getEncoded();
      assertFalse(Arrays.equals(key1, key2));
    }
  }

  @Test
  public void generateSecret_givenZeroLengthPassword_thenSuccess() {
    assertDoesNotThrow(() -> security.generateSecret(new char[0], security.generateSalt()));
  }

  @Test
  public void createCipher_givenGoodInput_thenValid() {
    assertDoesNotThrow(() -> {
      Cipher cipher = security.createCipher(true, "password123".toCharArray(), security.generateSalt());
      assertEquals(config.getFullCipherTransformation(), cipher.getAlgorithm());
    });
  }

  @Test
  public void createCipher_givenSameInput_thenDeterministic() throws Exception {
    byte[] salt = security.generateSalt();
    Cipher c1 = security.createCipher(true, "password123".toCharArray(), salt);

    byte[] message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.".getBytes(StandardCharsets.UTF_8);
    byte[] encodedMessage = c1.doFinal(message);

    Cipher c2 = security.createCipher(false, "password123".toCharArray(), salt);
    byte[] decodedMessage1 = c2.doFinal(encodedMessage);
    assertArrayEquals(message, decodedMessage1);

    Cipher c3 = security.createCipher(false, "password124".toCharArray(), salt);
    assertThrows(BadPaddingException.class, () -> c3.doFinal(encodedMessage));

    Cipher c4 = security.createCipher(false, "password123".toCharArray(), security.generateSalt());
    assertThrows(BadPaddingException.class, () -> c4.doFinal(encodedMessage));
  }

  @Test
  public void generateAuthToken_verify() {
    ComboAuthToken token = security.generateAuthToken("password123".toCharArray(), security.generateSalt());
    assertTrue(security.getAuthTokenCodec().verify(token));
    assertTrue(security.getAuthTokenCodec().verify(security.getAuthTokenCodec().encodeToString(token)));
  }

  @Test
  public void generateAuthToken_diffSaltsUniqueHashes() {
    ComboAuthToken t1 = security.generateAuthToken("password123".toCharArray(), security.generateSalt());
    for (int i = 0; i < 20; i++) {
      ComboAuthToken t2 = security.generateAuthToken("password123".toCharArray(), security.generateSalt());
      assertNotEquals(t1, t2);
    }
  }

}
