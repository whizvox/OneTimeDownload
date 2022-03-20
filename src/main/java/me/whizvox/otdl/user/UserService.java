package me.whizvox.otdl.user;

import me.whizvox.otdl.exception.EmailTakenException;
import me.whizvox.otdl.exception.InvalidPasswordException;
import me.whizvox.otdl.exception.TokenDoesNotExistException;
import me.whizvox.otdl.misc.EmptyJavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service
public class UserService implements UserDetailsService {

  private final UserRepository repo;
  private final ConfirmationTokenService tokens;
  private final PasswordEncoder encoder;
  private final JavaMailSender emailSender;
  private final UserConfigurationProperties config;

  private final Pattern passwordCheck;
  private final boolean shouldConfirmEmail;

  @Autowired
  public UserService(UserRepository repo, ConfirmationTokenService tokens, PasswordEncoder encoder, JavaMailSender emailSender, UserConfigurationProperties config) {
    this.repo = repo;
    this.tokens = tokens;
    this.encoder = encoder;
    this.emailSender = emailSender;
    this.config = config;
    //shouldConfirmEmail = !(emailSender instanceof EmptyJavaMailSender);
    shouldConfirmEmail = true;

    try {
      passwordCheck = Pattern.compile(config.getPasswordRequirementRegex());
    } catch (PatternSyntaxException e) {
      throw new RuntimeException("Invalid regex for password validation", e);
    }
  }

  public Optional<PublicUserDetails> getUserDetails(Long id) {
    return repo.findById(id).map(PublicUserDetails::new);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return repo
        .findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException(MessageFormat.format("Email {0} does not exist", username)));
  }

  public User registerNewUser(String email, CharSequence password) {
    if (!passwordCheck.matcher(password).matches()) {
      throw new InvalidPasswordException();
    }
    if (repo.findByEmail(email).isPresent()) {
      throw new EmailTakenException();
    }
    User user = User.builder().email(email).password(encoder.encode(password)).rank(UserRank.MEMBER).build();
    if (!shouldConfirmEmail) {
      user.setEnabled(true);
      repo.save(user);
      return user;
    }
    repo.save(user);
    ConfirmationToken token = new ConfirmationToken(user);
    tokens.store(token);
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(user.getEmail());
    msg.setFrom(config.getEmailFrom());
    msg.setSubject(config.getEmailSubject());
    msg.setText("Click here to confirm your account: " + config.getEmailHost() + "/confirm/" + token);
    emailSender.send(msg);
    return user;
  }

  public void confirmUser(String token) {
    Optional<ConfirmationToken> infoOp = tokens.getTokenInfo(token);
    if (infoOp.isEmpty()) {
      throw new TokenDoesNotExistException();
    }
    ConfirmationToken info = infoOp.get();
    User user = info.getUser();
    user.setEnabled(true);
    repo.save(user);
    tokens.delete(info.getId());
  }

}
