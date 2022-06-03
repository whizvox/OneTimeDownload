package me.whizvox.otdl.user;

import lombok.extern.slf4j.Slf4j;
import me.whizvox.otdl.exception.EmailTakenException;
import me.whizvox.otdl.exception.InvalidPasswordException;
import me.whizvox.otdl.exception.TokenDoesNotExistException;
import me.whizvox.otdl.exception.UnknownIdException;
import me.whizvox.otdl.misc.EmptyJavaMailSender;
import me.whizvox.otdl.util.StringUtils;
import me.whizvox.otdl.util.params.Parameters;
import me.whizvox.otdl.util.params.UpdateUserParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.StreamSupport;

@Service
@Slf4j
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
    // TODO Not a good way to check, should somehow include otdl.email.enable value
    shouldConfirmEmail = !(emailSender instanceof EmptyJavaMailSender);

    try {
      passwordCheck = Pattern.compile(config.getPasswordRequirementRegex());
    } catch (PatternSyntaxException e) {
      throw new RuntimeException("Invalid regex for password validation", e);
    }
  }

  public Optional<User> getUserDetails(UUID id) {
    return repo.findById(id);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return repo
        .findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException(MessageFormat.format("Email {0} does not exist", username)));
  }

  public boolean isEmailAvailable(String email) {
    return repo.findByEmail(email).isEmpty();
  }

  public User registerNewUser(String email, CharSequence password) {
    if (!passwordCheck.matcher(password).matches()) {
      throw new InvalidPasswordException();
    }
    if (!isEmailAvailable(email)) {
      throw new EmailTakenException();
    }
    User user = new User(UUID.randomUUID(), email, encoder.encode(password), UserRole.MEMBER, LocalDateTime.now(), false);
    if (!shouldConfirmEmail) {
      user.setVerified(true);
      repo.save(user);
      log.info("New user {} registered and enabled", user.getId());
      return user;
    }
    repo.save(user);
    ConfirmationToken token = new ConfirmationToken(user);
    tokens.store(token);
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(user.getEmail());
    msg.setFrom(config.getEmailFromAddress());
    msg.setSubject(config.getEmailSubject());
    msg.setText("Click here to confirm your account: " + config.getEmailConfirmHost() + "/confirm/" + token.getToken());
    emailSender.send(msg);
    log.info("New user {} registered, email sent to {} for verification", user.getId(), StringUtils.getObscuredEmail(user.getEmail()));
    return user;
  }

  public User createUser(String email, CharSequence password, UserRole role, boolean enabled) {
    if (!isEmailAvailable(email)) {
      throw new EmailTakenException();
    }
    User user = new User(UUID.randomUUID(), email, encoder.encode(password), role, LocalDateTime.now(), enabled);
    return repo.save(user);
  }

  public void confirmUser(String token) {
    Optional<ConfirmationToken> infoOp = tokens.getTokenInfo(token);
    if (infoOp.isEmpty()) {
      throw new TokenDoesNotExistException();
    }
    ConfirmationToken info = infoOp.get();
    User user = info.getUser();
    user.setVerified(true);
    repo.save(user);
    log.info("User {} has confirmed their email", user.getId());
    tokens.delete(info.getId());
  }

  public User update(UUID id, Parameters<User> params) {
    User user = repo.findById(id).orElseThrow(UnknownIdException::new);
    params.writeToEntity(user);

    if (repo.findByEmail(user.getEmail()).map(other -> !Objects.equals(other.getId(), user.getId())).orElse(false)) {
      throw new EmailTakenException();
    }
    if (params.containsValue(UpdateUserParameters.PASSWORD.getName())) {
      user.setPassword(encoder.encode(user.getPassword()));
    }
    User updatedUser = repo.save(user);
    log.info("User {} updated", id);
    return updatedUser;
  }

  public Page<User> search(Specification<User> spec, Pageable pageable) {
    return repo.findAll(spec, pageable);
  }

  public void delete(Iterable<UUID> ids) {
    repo.deleteAllById(ids);
    log.info("Deleted user(s): {}", StringUtils.limitedJoin(StreamSupport.stream(ids.spliterator(), false).map(UUID::toString), 10, ", "));
  }

  public long getCount() {
    return repo.count();
  }

  public long getUnverifiedCount() {
    return repo.countAllUnverified();
  }

}
