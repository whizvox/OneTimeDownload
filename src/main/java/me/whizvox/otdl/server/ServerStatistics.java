package me.whizvox.otdl.server;

import lombok.Data;

@Data
public class ServerStatistics {

  private long usersCount;
  private long usersUnverified;
  private long filesCount;
  private Long filesStorage;
  private long serverUptime;

}
