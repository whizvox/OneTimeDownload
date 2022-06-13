package me.whizvox.otdl.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Getter @Setter
@NoArgsConstructor
public class PasswordResetToken {

  @Id
  @Type(type = "uuid-char")
  private UUID id;

  private LocalDateTime expires;

  @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
  @JoinColumn(nullable = false, name = "user_id")
  private User user;

  public static PasswordResetToken create(User user, LocalDateTime expires) {
    PasswordResetToken token = new PasswordResetToken();
    token.setId(UUID.randomUUID());
    token.setUser(user);
    token.setExpires(expires);
    return token;
  }

}
