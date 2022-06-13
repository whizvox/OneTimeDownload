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
    return createEntity(HttpStatus.BAD_REQUEST, true, msg == null ? "Bad request" : msg);
  }

  public static ResponseEntity<Object> unauthorized() {
    return createEntity(HttpStatus.UNAUTHORIZED, true, "Unauthorized");
  }

  public static ResponseEntity<Object> forbidden(@Nullable String msg) {
    return createEntity(HttpStatus.FORBIDDEN, true, msg);
  }

  public static ResponseEntity<Object> notFound(@Nullable String path, @Nullable String type) {
    StringBuilder sb = new StringBuilder("Resource not found");
    if (type != null) {
      sb.append(" (").append(type).append(")");
    }
    if (path != null) {
      sb.append(": ").append(path);
    }
    return createEntity(HttpStatus.NOT_FOUND, true, sb.toString());
  }

  public static ResponseEntity<Object> notFound(@Nullable String path) {
    return notFound(path, null);
  }

  public static ResponseEntity<Object> conflict(@Nullable String msg) {
    return createEntity(HttpStatus.CONFLICT, true, msg);
  }

  public static ResponseEntity<Object> internalServerError(@Nullable String msg) {
    return createEntity(HttpStatus.INTERNAL_SERVER_ERROR, true, msg);
  }

}
