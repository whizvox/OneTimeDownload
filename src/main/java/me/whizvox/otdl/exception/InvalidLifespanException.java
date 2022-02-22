package me.whizvox.otdl.exception;

public class InvalidLifespanException extends OTDLServiceException {

  public InvalidLifespanException(int lifespan) {
    super("Bad lifespan: " + lifespan + " minutes");
  }

}
