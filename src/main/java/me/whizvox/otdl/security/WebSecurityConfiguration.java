package me.whizvox.otdl.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.whizvox.otdl.user.UserService;
import me.whizvox.otdl.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.MimeTypeUtils;

@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final UserService users;
  private final PasswordEncoder passwordEncoder;
  private final ObjectMapper objectMapper;
  private final SecurityConfiguration config;

  @Autowired
  public WebSecurityConfiguration(UserService users, PasswordEncoder passwordEncoder, ObjectMapper objectMapper, SecurityConfiguration config) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.objectMapper = objectMapper;
    this.config = config;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    HttpSecurity security = http.authorizeRequests()
        .antMatchers("/debug/**", "/control/**", "/server/**")
            .hasRole("ADMIN")
            .anyRequest()
            .permitAll()
            .and()
        .formLogin()
            .loginPage("/login")
            .defaultSuccessUrl("/")
            .failureHandler(new OTDLLoginFailureHandler())
            .and()
        .logout()
            .logoutUrl("/logout")
            .logoutSuccessHandler((request, response, authentication) -> {
              response.setContentType(MimeTypeUtils.APPLICATION_JSON.toString());
              objectMapper.writeValue(response.getWriter(), ApiResponse.ok());
            })
            .and();
    if (config.isEnableCsrf()) {
      security = security.csrf().and();
    } else {
      security = security.csrf().disable();
    }
    if (config.isRememberMe()) {
      security = security.rememberMe().and();
    }
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(users).passwordEncoder(passwordEncoder);
  }

}
