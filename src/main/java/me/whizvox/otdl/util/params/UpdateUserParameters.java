package me.whizvox.otdl.util.params;

import me.whizvox.otdl.user.User;
import me.whizvox.otdl.user.UserRole;

import java.util.Map;

public class UpdateUserParameters extends Parameters<User> {

  private static final String
      KEY_EMAIL = "email",
      KEY_PASSWORD = "password",
      KEY_ROLE = "role",
      KEY_VERIFIED = "enabled";

  public static final Parameter<String, User> EMAIL = Parameter.<String, User>builder()
      .name(KEY_EMAIL)
      .setter(User::setEmail)
      .getter(User::getEmail)
      .decoder(ParameterDecoders.STRING)
      .build();

  public static final Parameter<String, User> PASSWORD = Parameter.<String, User>builder()
      .name(KEY_PASSWORD)
      .setter(User::setPassword)
      .getter(User::getPassword)
      .decoder(ParameterDecoders.STRING)
      .build();

  public static final Parameter<UserRole, User> ROLE = Parameter.<UserRole, User>builder()
      .name(KEY_ROLE)
      .setter(User::setRole)
      .getter(User::getRole)
      .decoder(ParameterDecoders.ofEnum(UserRole.class))
      .build();

  public static final Parameter<Boolean, User> VERIFIED = Parameter.<Boolean, User>builder()
      .name(KEY_VERIFIED)
      .setter(User::setVerified)
      .getter(User::isVerified)
      .decoder(ParameterDecoders.BOOLEAN)
      .build();

  private static final Map<String, Parameter<?, User>> PARAMETERS = Parameters.<User>builder()
      .add(EMAIL)
      .add(PASSWORD)
      .add(ROLE)
      .add(VERIFIED)
      .build();

  public UpdateUserParameters(Map<String, Object> defaultValues) {
    super(PARAMETERS, defaultValues);
  }

  public UpdateUserParameters() {
    this(Map.of());
  }

  public UpdateUserParameters email(String email) {
    set(KEY_EMAIL, email);
    return this;
  }

  public UpdateUserParameters password(String password) {
    set(KEY_PASSWORD, password);
    return this;
  }

  public UpdateUserParameters role(UserRole role) {
    set(KEY_ROLE, role);
    return this;
  }

  public UpdateUserParameters verified(boolean verified) {
    set(KEY_VERIFIED, verified);
    return this;
  }

}
