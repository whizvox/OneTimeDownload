package me.whizvox.otdl.file;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
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

  private boolean downloaded;

}
