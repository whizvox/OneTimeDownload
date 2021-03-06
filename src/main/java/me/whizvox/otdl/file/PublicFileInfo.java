package me.whizvox.otdl.file;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class PublicFileInfo {

  public PublicFileInfo(FileInfo info) {
    setId(info.getId());
    setFileName(info.getFileName());
    setUploaded(info.getUploaded());
    setOriginalSize(info.getOriginalSize());
    setExpires(info.getExpires());
    setDownloaded(info.isDownloaded());
  }

  private String id;

  private String fileName;

  private LocalDateTime uploaded;

  private long originalSize;

  private LocalDateTime expires;

  private boolean downloaded;

}
