package me.whizvox.otdl.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "confirmation_tokens")
@Getter @Setter
@NoArgsConstructor
public class ConfirmationToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String token;

  private LocalDateTime created;

  @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
  @JoinColumn(nullable = false, name = "user_id")
  private User user;

  public ConfirmationToken(User user) {
    this.user = user;
    created = LocalDateTime.now();
    token = UUID.randomUUID().toString();
  }

  public static final ConfirmationToken EMPTY = new ConfirmationToken();

}
