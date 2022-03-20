package me.whizvox.otdl.misc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfiguration {

  @Value("${otdl.security.bcrypt.strength:6}")
  private int strength;

  @Value("${otdl.security.bcrypt.version:2A}")
  private BCryptPasswordEncoder.BCryptVersion version;

  @Bean
  public PasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder(version, strength);
  }

}
