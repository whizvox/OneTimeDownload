package me.whizvox.otdl.exception;

public class WrongPasswordException extends OTDLServiceException {

  public WrongPasswordException() {
    super("Unauthorized access: incorrect password");
  }

}
