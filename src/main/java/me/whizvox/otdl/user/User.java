package me.whizvox.otdl.user;

import lombok.*;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User implements UserDetails {

  @Id
  @Type(type = "uuid-char")
  private UUID id;

  private String email;

  private String password;

  @Enumerated(EnumType.STRING)
  private UserRole role;

  private LocalDateTime created;

  private boolean verified;

  public User(String email, String password, UserRole role, boolean verified) {
    this(UUID.randomUUID(), email, password, role, LocalDateTime.now(), verified);
  }

  public User(String email, String password) {
    this(email, password, UserRole.MEMBER, false);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return role != UserRole.RESTRICTED;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return verified;
  }

  public boolean isGuest() {
    return id == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return
        verified == user.verified &&
        Objects.equals(id, user.id) &&
        Objects.equals(email, user.email) &&
        Objects.equals(password, user.password) &&
        role == user.role &&
        Objects.equals(created, user.created);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, email, password, role, created, verified);
  }

  public static User guest() {
    return new User(null, null, null, UserRole.GUEST, null, true);
  }

}
