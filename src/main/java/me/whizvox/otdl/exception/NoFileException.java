package me.whizvox.otdl.exception;

public class NoFileException extends OTDLServiceException {

  public NoFileException() {
    super("Missing file");
  }

}
