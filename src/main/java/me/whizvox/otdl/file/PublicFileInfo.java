package me.whizvox.otdl.file;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class PublicFileInfo {

  public PublicFileInfo(FileInfo info) {
    setId(info.getId());
    setUploaded(info.getUploaded());
    setOriginalSize(info.getOriginalSize());
    setExpires(info.getExpires());
  }

  @Getter @Setter
  private String id;

  @Getter @Setter
  private LocalDateTime uploaded;

  @Getter @Setter
  private long originalSize;

  @Getter @Setter
  private LocalDateTime expires;

}
