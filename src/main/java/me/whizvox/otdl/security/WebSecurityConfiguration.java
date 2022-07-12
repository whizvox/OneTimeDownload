package me.whizvox.otdl.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.whizvox.otdl.user.UserService;
import me.whizvox.otdl.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.util.MimeTypeUtils;

import javax.sql.DataSource;

@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final UserService users;
  private final PasswordEncoder passwordEncoder;
  private final ObjectMapper objectMapper;
  private final SecurityConfiguration config;
  private final DataSource dataSource;

  @Autowired
  public WebSecurityConfiguration(UserService users,
                                  PasswordEncoder passwordEncoder,
                                  ObjectMapper objectMapper,
                                  SecurityConfiguration config,
                                  DataSource dataSource) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.objectMapper = objectMapper;
    this.config = config;
    this.dataSource = dataSource;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    HttpSecurity security = http.authorizeRequests()
        .antMatchers("/control/**", "/server/**")
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
            .and()
        .rememberMe()
          .tokenRepository(persistentTokenRepository())
          //.rememberMeCookieDomain("1tdl.com")
          //.useSecureCookie(true) // maybe enable this over https?
          .tokenValiditySeconds(60 * 60 * 24 * 7) // 1 week
          .and();
    if (config.isEnableCsrf()) {
      security = security.csrf().and();
    } else {
      security = security.csrf().disable();
    }
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(users).passwordEncoder(passwordEncoder);
  }

  @Bean
  public PersistentTokenRepository persistentTokenRepository() {
    JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
    repo.setDataSource(dataSource);
    return repo;
  }

}
