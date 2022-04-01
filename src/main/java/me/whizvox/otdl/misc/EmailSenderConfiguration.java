package me.whizvox.otdl.misc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailSenderConfiguration {

  @Value("${otdl.email.enable:false}")
  private Boolean enable;

  @Value("${otdl.email.host:#{null}}")
  private String host;

  @Value("${otdl.email.port:0}")
  private int port;

  @Value("${otdl.email.username:#{null}}")
  private String username;

  @Value("${otdl.email.password:#{null}}")
  private String password;

  @Value("${otdl.email.protocol:smtp}")
  private String protocol;

  @Value("${otdl.email.encoding:UTF-8}")
  private String encoding;

  /*@Value("${otdl.email.tls:true}")
  private boolean tls;*/

  @Value("${otdl.email.start-tls:true}")
  private boolean startTLS;

  @Value("${otdl.email.ssl.trust:${otdl.email.host:#{null}}}")
  private String sslTrustHost;

  @Bean
  public JavaMailSender javaMailSender() {
    if (enable) {
      JavaMailSenderImpl sender = new JavaMailSenderImpl();
      sender.setHost(host);
      sender.setPort(port);
      sender.setUsername(username);
      sender.setPassword(password);
      sender.setProtocol(protocol);
      sender.setDefaultEncoding(encoding);
      sender.getJavaMailProperties().setProperty("mail.smtp.auth", "true");
      sender.getJavaMailProperties().setProperty("mail.smtp.starttls.enable", Boolean.toString(startTLS));
      sender.getJavaMailProperties().setProperty("mail.smtp.ssl.trust", sslTrustHost);
      return sender;
    }
    return new EmptyJavaMailSender();
  }

}
