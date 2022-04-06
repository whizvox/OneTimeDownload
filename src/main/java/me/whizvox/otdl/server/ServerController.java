package me.whizvox.otdl.server;

import me.whizvox.otdl.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/server")
public class ServerController {

  private ServerService server;

  @Autowired
  public ServerController(ServerService server) {
    this.server = server;
  }

  @GetMapping("stats")
  public ResponseEntity<Object> getStats() {
    return ApiResponse.ok(server.getStats());
  }

  @PostMapping("restart")
  public ResponseEntity<Object> restart() {
    server.restart();
    return ApiResponse.ok();
  }

  @PostMapping("shutdown")
  public ResponseEntity<Object> shutdown() {
    try {
      server.shutdown();
      return ApiResponse.ok();
    } catch (IllegalStateException e) {
      return ApiResponse.internalServerError(e.getMessage());
    }
  }

}
