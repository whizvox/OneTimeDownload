package me.whizvox.otdl.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

  @Getter @Setter
  private int status;

  @Getter @Setter
  private boolean error;

  @Getter @Setter
  private Object data;

  @NoArgsConstructor
  @AllArgsConstructor
  public static class ErrorData {

    @Getter @Setter
    private String message;

  }

  @NoArgsConstructor
  public static class NotFoundData extends ErrorData {

    @Getter @Setter
    private String path;

    public NotFoundData(String message, String path) {
      super(message);
      this.path = path;
    }

  }

  private static ResponseEntity<Object> createEntity(HttpStatus status, boolean error, Object data) {
    return new ResponseEntity<>(new ApiResponse(status.value(), error, data), status);
  }

  public static ResponseEntity<Object> ok(Object data) {
    return createEntity(HttpStatus.OK, false, data);
  }

  public static ResponseEntity<Object> ok() {
    return ok(null);
  }

  public static ResponseEntity<Object> badRequest(@Nullable String msg) {
    return createEntity(HttpStatus.BAD_REQUEST, true, new ErrorData(msg == null ? "Bad request" : msg));
  }

  public static ResponseEntity<Object> unauthorized() {
    return createEntity(HttpStatus.UNAUTHORIZED, true, new ErrorData("Unauthorized"));
  }

  public static ResponseEntity<Object> notFound(@Nullable String path) {
    return createEntity(HttpStatus.NOT_FOUND, true, new NotFoundData("Resource not found", path));
  }

  public static ResponseEntity<Object> internalServerError(@Nullable String msg) {
    return createEntity(HttpStatus.INTERNAL_SERVER_ERROR, true, msg);
  }

}
