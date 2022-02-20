package me.whizvox.otdl.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

@Service
public class DefaultSecurityService implements SecurityService {

  private final SecurityConfiguration config;
  private final ComboAuthToken.Codec codec;

  @Autowired
  public DefaultSecurityService(SecurityConfiguration config) {
    this.config = config;
    codec = new ComboAuthToken.Codec() {
      final int HASH_LENGTH = config.getKeyLength() / 8;
      final int SALT_LENGTH = config.getSaltSize();
      final int TOTAL_LENGTH = HASH_LENGTH + SALT_LENGTH;
      @Override
      public boolean verify(byte[] bytes) {
        return bytes.length == TOTAL_LENGTH;
      }
      @Override
      public ComboAuthToken decode(byte[] bytes) {
        byte[] hash = new byte[HASH_LENGTH];
        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy(bytes, 0, hash, 0, hash.length);
        System.arraycopy(bytes, hash.length, salt, 0, salt.length);
        return new ComboAuthToken(hash, salt);
      }
      @Override
      public byte[] encode(byte[] hash, byte[] salt) {
        byte[] result = new byte[hash.length + salt.length];
        System.arraycopy(hash, 0, result, 0, hash.length);
        System.arraycopy(salt, 0, result, hash.length, salt.length);
        return result;
      }
    };
  }

  @Override
  public byte[] generateSalt() {
    byte[] salt = new byte[config.getSaltSize()];
    new SecureRandom().nextBytes(salt);
    return salt;
  }

  @Override
  public SecretKey generateSecret(char[] password, byte[] salt) {
    try {
      SecretKeyFactory skf = SecretKeyFactory.getInstance(config.getPasswordHashAlgorithm());
      KeySpec spec = new PBEKeySpec(password, salt, config.getIterationCount(), config.getKeyLength());
      return skf.generateSecret(spec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Cipher createCipher(boolean shouldEncrypt, char[] password, byte[] salt) {
    try {
      byte[] key = new byte[password.length * 2 + salt.length];
      for (int i = 0; i < password.length; i++) {
        key[i * 2] = (byte) (password[i] & 0xFF00);
        key[i * 2 + 1] = (byte) password[i];
      }
      System.arraycopy(salt, 0, key, password.length * 2, salt.length);
      SecretKey sk = new SecretKeySpec(key, config.getCipherAlgorithm());
      Arrays.fill(key, (byte) 0);
      Cipher cipher = Cipher.getInstance(config.getFullCipherTransformation());
      cipher.init(shouldEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, sk);
      return cipher;
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ComboAuthToken.Codec getAuthTokenCodec() {
    return codec;
  }

}
