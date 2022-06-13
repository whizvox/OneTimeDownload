package me.whizvox.otdl.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otdl.user")
@Getter @Setter
public class UserConfigurationProperties {

  private String passwordRequirementRegex = "^.{8,}$";

  private String passwordRequirementDescription = "Must be at least 8 characters";

  private String emailFromAddress;

  private String emailSubject = "Confirm your email address";

  private String emailConfirmHost;

  private int verificationLinkLifetime = 60;

  private int passwordResetLinkLifetime = 10;

}
