package me.whizvox.otdl.util;

import java.time.Duration;

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

}
