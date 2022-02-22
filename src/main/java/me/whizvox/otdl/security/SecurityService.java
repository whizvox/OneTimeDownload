package me.whizvox.otdl.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public interface SecurityService {

  byte[] generateSalt();

  SecretKey generateSecret(char[] password, byte[] salt);

  Cipher createCipher(boolean shouldEncrypt, char[] password, byte[] salt);

  ComboAuthToken.Codec getAuthTokenCodec();

  default ComboAuthToken generateAuthToken(char[] password, byte[] salt) {
    return new ComboAuthToken(generateSecret(password, salt).getEncoded(), salt);
  }

  default boolean authenticate(String authTokenStr, char[] password) {
    ComboAuthToken token = getAuthTokenCodec().decode(authTokenStr);
    return token.authorize(generateSecret(password, token.getSalt()).getEncoded());
  }

}
