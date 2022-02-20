package me.whizvox.otdl.exception;

public class OTDLServiceException extends RuntimeException {

  public OTDLServiceException() {
  }

  public OTDLServiceException(String message) {
    super(message);
  }

  public OTDLServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public OTDLServiceException(Throwable cause) {
    super(cause);
  }

}
