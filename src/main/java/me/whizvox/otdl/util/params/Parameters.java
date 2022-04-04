package me.whizvox.otdl.util.params;

import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Parameters<ENTITY> {

  private final Map<String, Parameter<?, ENTITY>> parameters;
  private final Map<String, Object> values;

  public Parameters(Map<String, Parameter<?, ENTITY>> parameters, @Nullable Map<String, Object> defaultValues) {
    this.parameters = Collections.unmodifiableMap(parameters);
    if (defaultValues != null && !defaultValues.isEmpty()) {
      this.values = new HashMap<>(defaultValues);
    } else {
      this.values = new HashMap<>();
    }
  }

  public Parameters(Map<String, Parameter<?, ENTITY>> parameters) {
    this(parameters, null);
  }

  @SuppressWarnings("unchecked")
  private <PARAM> Parameter<PARAM, ENTITY> getParameter(String key) {
    if (!parameters.containsKey(key)) {
      throw new ParameterDeserializationException("Unknown parameter: " + key);
    }
    try {
      return (Parameter<PARAM, ENTITY>) parameters.get(key);
    } catch (ClassCastException e) {
      throw new ParameterDeserializationException("Improper cast to parameter " + key);
    }
  }

  private <PARAM> PARAM deserialize(String key, Object param) {
    Parameter<PARAM, ENTITY> parameter = getParameter(key);
    if (param == null) {
      return null;
    }
    return parameter.deserialize(param);
  }

  public boolean containsValue(String key) {
    return values.containsKey(key);
  }

  protected <PARAM> PARAM get(String key, PARAM defaultValue) {
    return deserialize(key, values.getOrDefault(key, defaultValue));
  }

  protected <PARAM> PARAM get(String key) {
    if (values.containsKey(key)) {
      return deserialize(key, values.get(key));
    }
    throw new ParameterDeserializationException("Missing parameter: " + key);
  }

  public <PARAM> void set(String key, PARAM param) {
    Parameter<PARAM, ENTITY> parameter = getParameter(key);
    values.put(key, parameter.serialize(param));
  }

  public void setAll(Map<String, String> params) {
    params.forEach((key, value) -> {
      if (parameters.containsKey(key)) {
        Parameter<?, ENTITY> parameter = parameters.get(key);
        set(key, parameter.decode(value));
      }
    });
  }

  public void setAll(MultiValueMap<String, String> params) {
    params.forEach((key, values) -> {
      if (parameters.containsKey(key)) {
        Parameter<?, ENTITY> parameter = parameters.get(key);
        set(key, parameter.decode(values.get(0)));
      }
    });
  }

  public <T> void writeToField(String key, ENTITY entity) {
    Parameter<T, ENTITY> parameter = getParameter(key);
    if (parameter.isOptional()) {
      if (containsValue(key)) {
        parameter.set(entity, parameter.deserialize(values.get(key)));
      }
    } else {
      if (parameter.isHasDefault()) {
        parameter.set(entity, get(key, parameter.getDefaultSupplier().get()));
      } else {
        parameter.set(entity, get(key));
      }
    }
  }

  public void writeToEntity(ENTITY entity) {
    parameters.forEach((key, parameter) -> writeToField(key, entity));
  }

  public String encodeToString() {
    return values.entrySet().stream().map(entry -> {
      Parameter<?, ENTITY> parameter = parameters.get(entry.getKey());
      return entry.getKey() + "=" + URLEncoder.encode(parameter.deserializeAndEncode(entry.getValue()), StandardCharsets.UTF_8);
    }).collect(Collectors.joining("&"));
  }

  public static <ENTITY> Builder<ENTITY> builder() {
    return new Builder<>();
  }

  public static class Builder<ENTITY> {
    private final HashMap<String, Parameter<?, ENTITY>> parameters;

    public Builder() {
      parameters = new HashMap<>();
    }

    public Builder<ENTITY> add(Parameter<?, ENTITY> parameter) {
      parameters.put(parameter.getName(), parameter);
      return this;
    }

    public Map<String, Parameter<?, ENTITY>> build() {
      return Collections.unmodifiableMap(parameters);
    }

  }

}
