package me.whizvox.otdl.util.params;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Function;

public class ParameterDecoders {

  // trivial types
  public static final ParameterDecoder<String> STRING = s -> s;
  public static final ParameterDecoder<Integer> INTEGER = Integer::valueOf;
  public static final ParameterDecoder<Long> LONG = Long::valueOf;
  public static final ParameterDecoder<UUID> $UUID = UUID::fromString;

  // non-trivial types
  public static final ParameterDecoder<Boolean> BOOLEAN = s -> {
    if ("0".equals(s) || "false".equalsIgnoreCase(s)) {
      return false;
    } else if ("1".equals(s) || "true".equalsIgnoreCase(s)) {
      return true;
    }
    throw new ParameterDeserializationException("Invalid boolean: " + s);
  };
  public static final ParameterDecoder<LocalDateTime> LOCAL_DATE_TIME = s ->
      LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(s));

  public static <T extends Enum<T>> ParameterDecoder<T> ofEnum(Class<T> cls) {
    return s -> Enum.valueOf(cls, s.toLowerCase());
  }

  public interface ParameterDecoder<PARAM> extends Function<String, PARAM> {
  }

}
