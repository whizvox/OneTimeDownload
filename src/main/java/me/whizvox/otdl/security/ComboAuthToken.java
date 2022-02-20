package me.whizvox.otdl.security;

import java.util.Arrays;
import java.util.HexFormat;

public class ComboAuthToken {

  private final byte[] hash;
  private final byte[] salt;

  public ComboAuthToken(byte[] hash, byte[] salt) {
    this.hash = hash.clone();
    this.salt = salt.clone();
  }

  public byte[] getHash() {
    return hash.clone();
  }

  public byte[] getSalt() {
    return salt.clone();
  }

  public boolean authorize(byte[] attemptHash) {
    return Arrays.equals(attemptHash, hash);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ComboAuthToken that = (ComboAuthToken) o;
    return Arrays.equals(hash, that.hash) && Arrays.equals(salt, that.salt);
  }

  public interface Codec {

    boolean verify(byte[] bytes);

    default boolean verify(String str) {
      return verify(HexFormat.of().parseHex(str));
    }

    default boolean verify(ComboAuthToken token) {
      return verify(encode(token));
    }

    ComboAuthToken decode(byte[] bytes);

    default ComboAuthToken decode(String str) {
      return decode(HexFormat.of().parseHex(str));
    }

    byte[] encode(byte[] hash, byte[] salt);

    default byte[] encode(ComboAuthToken token) {
      return encode(token.getHash(), token.getSalt());
    }

    default String encodeToString(byte[] hash, byte[] salt) {
      return HexFormat.of().formatHex(encode(hash, salt));
    }

    default String encodeToString(ComboAuthToken token) {
      return encodeToString(token.getHash(), token.getSalt());
    }

  }

}
