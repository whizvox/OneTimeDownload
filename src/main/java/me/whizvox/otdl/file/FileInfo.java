package me.whizvox.otdl.file;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class FileInfo {

  @Id
  @Getter @Setter
  private String id;

  @Getter @Setter
  private String authToken;

  @Getter @Setter
  private LocalDateTime uploaded;

  @Getter @Setter
  private String md5;

  @Getter @Setter
  private String sha1;

  @Getter @Setter
  private long originalSize;

  @Getter @Setter
  private long storedSize;

}
