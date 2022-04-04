package me.whizvox.otdl.util.params;

import me.whizvox.otdl.user.User;
import me.whizvox.otdl.user.UserGroup;
import me.whizvox.otdl.user.UserRank;

import java.util.Map;

public class UpdateUserParameters extends Parameters<User> {

  private static final String
      KEY_EMAIL = "email",
      KEY_PASSWORD = "password",
      KEY_RANK = "rank",
      KEY_GROUP = "group",
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

  public static final Parameter<UserRank, User> RANK = Parameter.<UserRank, User>builder()
      .name(KEY_RANK)
      .setter(User::setRank)
      .getter(User::getRank)
      .decoder(ParameterDecoders.ofEnum(UserRank.class))
      .build();

  public static final Parameter<UserGroup, User> GROUP = Parameter.<UserGroup, User>builder()
      .name(KEY_GROUP)
      .setter(User::setGroup)
      .getter(User::getGroup)
      .decoder(ParameterDecoders.ofEnum(UserGroup.class))
      .build();

  public static final Parameter<Boolean, User> VERIFIED = Parameter.<Boolean, User>builder()
      .name(KEY_VERIFIED)
      .setter(User::setEnabled)
      .getter(User::isEnabled)
      .decoder(ParameterDecoders.BOOLEAN)
      .build();

  private static final Map<String, Parameter<?, User>> PARAMETERS = Parameters.<User>builder()
      .add(EMAIL)
      .add(PASSWORD)
      .add(RANK)
      .add(GROUP)
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

  public UpdateUserParameters rank(UserRank rank) {
    set(KEY_RANK, rank);
    return this;
  }

  public UpdateUserParameters group(UserGroup group) {
    set(KEY_GROUP, group);
    return this;
  }

  public UpdateUserParameters verified(boolean verified) {
    set(KEY_VERIFIED, verified);
    return this;
  }

}
