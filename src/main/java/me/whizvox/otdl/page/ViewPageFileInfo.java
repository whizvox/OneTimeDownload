package me.whizvox.otdl.page;

import lombok.Getter;
import lombok.Setter;
import me.whizvox.otdl.file.FileInfo;
import me.whizvox.otdl.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter @Setter
public class ViewPageFileInfo {

  public ViewPageFileInfo(FileInfo info) {
    setId(info.getId());
    setFileName(info.getFileName());
    setOriginalSize(StringUtils.formatBytes(info.getOriginalSize()));
    setUploaded(info.getUploaded().format(DateTimeFormatter.ISO_DATE_TIME));
    setExpiresIn(StringUtils.formatDuration(Duration.between(LocalDateTime.now(), info.getExpires())));
    setDownloaded(info.isDownloaded());
  }

  private String id;

  private String fileName;

  private String originalSize;

  private String uploaded;

  private String expiresIn;

  private boolean downloaded;

}
