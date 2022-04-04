package me.whizvox.otdl.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtils {

  private static final String[] BINARY_UNIT_PREFIXES = {"B", "KB", "MB", "GB", "TB", "EB", "PB"};

  public static String formatBytes(long n) {
    if (Math.abs(n) < 1000) {
      return n + " B";
    }
    boolean negative = n < 0;
    n = Math.abs(n);
    int unitIndex = 1;
    while (n > 1_000_000 || unitIndex >= BINARY_UNIT_PREFIXES.length - 1) {
      n /= 1000;
      unitIndex++;
    }
    StringBuilder sb = new StringBuilder();
    if (negative) {
      sb.append('-');
    }
    sb.append(String.format("%.1f", n / 1000.0F)).append(' ').append(BINARY_UNIT_PREFIXES[unitIndex]);
    return sb.toString();
  }

  public static String formatDuration(Duration duration) {
    long millis = duration.toMillis();
    StringBuilder sb = new StringBuilder();
    if (millis < 0) {
      sb.append('-');
      millis *= -1;
    }
    if (millis > 86400000) {
      sb.append(millis / 86400000).append("d");
      millis %= 86400000;
    }
    if (millis > 3600000) {
      sb.append(millis / 3600000).append("h");
      millis %= 3600000;
    }
    if (millis > 60000) {
      sb.append(millis / 60000).append("m");
    }
    return sb.toString();
  }

  public static String[] split(String str, char c, int max) {
    ArrayList<String> words = new ArrayList<>();
    int last = 0;
    for (int i = 0; i < str.length() && words.size() < max; i++) {
      if (str.charAt(i) == c) {
        words.add(str.substring(last, i));
        last = i + 1;
      }
    }
    words.add(str.substring(last));
    return words.toArray(new String[0]);
  }

  public static String[] split(String str, char c) {
    return split(str, c, Integer.MAX_VALUE);
  }

  public static String getObscuredEmail(String email) {
    String[] words = split(email, '@', 2);
    if (words.length < 2) {
      throw new IllegalArgumentException("Invalid email address");
    }
    String first = words[0];
    if (first.length() > 6) {
      return first.substring(0, 2) + "***" + first.substring(first.length() - 2) + "@" + words[1];
    } else if (first.length() > 3) {
      return first.charAt(0) + "***" + first.substring(first.length() - 1) + "@" + words[1];
    }
    return "***@" + words[1];
  }

  public static String limitedJoin(Stream<String> strings, int max, String delimiter) {
    List<String> list = strings.collect(Collectors.toList());
    if (list.size() > max) {
      return list.stream().limit(max).collect(Collectors.joining(delimiter)) + "... (" + (list.size() - max) + " more)";
    }
    return String.join(delimiter, list);
  }

}
