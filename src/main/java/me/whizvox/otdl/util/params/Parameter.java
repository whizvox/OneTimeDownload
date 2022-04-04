package me.whizvox.otdl.util.params;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.thymeleaf.engine.ElementName;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Parameter<PARAM, DATA> {

  @Getter
  private final String name;

  @Getter
  private final Supplier<PARAM> defaultSupplier;

  @Getter
  private final boolean hasDefault;

  @Getter
  private final boolean optional;

  private final BiConsumer<DATA, PARAM> setter;

  private final Function<DATA, PARAM> getter;

  private final Function<PARAM, Object> internalSerializer;

  private final Function<Object, PARAM> internalDeserializer;

  private final Function<PARAM, String> encoder;

  private final Function<String, PARAM> decoder;

  public void set(DATA data, PARAM obj) {
    setter.accept(data, obj);
  }

  public PARAM get(DATA data) {
    return getter.apply(data);
  }

  public Object serialize(PARAM obj) {
    return internalSerializer.apply(obj);
  }

  public PARAM deserialize(Object obj) {
    return internalDeserializer.apply(obj);
  }

  public String encode(PARAM obj) {
    return encoder.apply(obj);
  }

  public PARAM decode(String str) {
    return decoder.apply(str);
  }

  public String deserializeAndEncode(Object obj) {
    return encode(internalDeserializer.apply(obj));
  }

  public void deserializeAndSet(DATA data, Object obj) {
    set(data, internalDeserializer.apply(obj));
  }

  public static <PARAM, ENTITY> Builder<PARAM, ENTITY> builder() {
    return new Builder<>();
  }

  public static class Builder<PARAM, ENTITY> {
    // optional attributes
    private Supplier<PARAM> defaultSupplier;
    private boolean hasDefault;
    private boolean optional;
    private Function<PARAM, Object> internalSerializer;
    private Function<Object, PARAM> internalDeserializer;
    private Function<PARAM, String> encoder;

    // required attributes
    private String name;
    private BiConsumer<ENTITY, PARAM> setter;
    private Function<ENTITY, PARAM> getter;
    private Function<String, PARAM> decoder;

    @SuppressWarnings("unchecked")
    public Builder() {
      defaultSupplier = () -> null;
      hasDefault = false;
      optional = false;
      internalSerializer = param -> param;
      internalDeserializer = obj -> {
        if (obj == null) {
          return null;
        }
        try {
          return (PARAM) obj;
        } catch (ClassCastException e) {
          throw new ParameterDeserializationException("Incompatible type: " + obj.getClass());
        }
      };
      encoder = String::valueOf;

      name = null;
      setter = null;
      getter = null;
      decoder = null;
    }

    public Builder<PARAM, ENTITY> defaultSupplier(@NonNull Supplier<PARAM> defaultSupplier) {
      this.defaultSupplier = defaultSupplier;
      return this;
    }

    public Builder<PARAM, ENTITY> hasDefault(boolean supplyDefault) {
      this.hasDefault = supplyDefault;
      return this;
    }

    public Builder<PARAM, ENTITY> optional(boolean optional) {
      this.optional = optional;
      return this;
    }

    public Builder<PARAM, ENTITY> serializer(@NonNull Function<PARAM, Object> internalSerializer) {
      this.internalSerializer = internalSerializer;
      return this;
    }

    public Builder<PARAM, ENTITY> deserializer(@NonNull Function<Object, PARAM> internalDeserializer) {
      this.internalDeserializer = internalDeserializer;
      return this;
    }

    public Builder<PARAM, ENTITY> encoder(@NonNull Function<PARAM, String> encoder) {
      this.encoder = encoder;
      return this;
    }

    public Builder<PARAM, ENTITY> name(@NonNull String name) {
      this.name = name;
      return this;
    }

    public Builder<PARAM, ENTITY> setter(@NonNull BiConsumer<ENTITY, PARAM> setter) {
      this.setter = setter;
      return this;
    }

    public Builder<PARAM, ENTITY> getter(@NonNull Function<ENTITY, PARAM> getter) {
      this.getter = getter;
      return this;
    }

    public Builder<PARAM, ENTITY> decoder(@NonNull Function<String, PARAM> decoder) {
      this.decoder = decoder;
      return this;
    }

    public Parameter<PARAM, ENTITY> build() {
      if (name == null || setter == null || getter == null || decoder == null) {
        throw new ParameterBuilderException("Missing one of required attributes: name, setter, getter, decoder");
      }
      return new Parameter<>(name, defaultSupplier, hasDefault, optional, setter, getter, internalSerializer, internalDeserializer, encoder, decoder);
    }

  }

}
