package me.whizvox.otdl.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens")
@Getter @Setter
@NoArgsConstructor
public class EmailVerificationToken {

  @Id
  @Type(type = "uuid-char")
  private UUID id;

  private String token;

  private LocalDateTime created;

  @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
  @JoinColumn(nullable = false, name = "user_id")
  private User user;

  public EmailVerificationToken(User user) {
    this.user = user;
    id = UUID.randomUUID();
    created = LocalDateTime.now();
    token = UUID.randomUUID().toString();
  }

}
