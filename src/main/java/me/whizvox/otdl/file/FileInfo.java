package me.whizvox.otdl.file;

import lombok.Getter;
import lombok.Setter;
import me.whizvox.otdl.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter @Setter
public class FileInfo {

  @Id
  private String id;

  private String fileName;

  private String authToken;

  private LocalDateTime uploaded;

  private String md5;

  private String sha1;

  private long originalSize;

  private long storedSize;

  private LocalDateTime expires;

  private int lifespanAfterAccess;

  private boolean downloaded;

  @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id")
  private User user;

}
