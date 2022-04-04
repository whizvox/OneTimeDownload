package me.whizvox.otdl.util.params;

import me.whizvox.otdl.exception.OTDLServiceException;

public class ParameterDeserializationException extends OTDLServiceException {

  public ParameterDeserializationException(String message) {
    super(message);
  }

  public ParameterDeserializationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ParameterDeserializationException(Throwable cause) {
    super(cause);
  }

}
