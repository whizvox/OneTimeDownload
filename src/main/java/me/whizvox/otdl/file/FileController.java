package me.whizvox.otdl.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("files")
public class FileController {

  private FileService files;

  @Autowired
  public FileController(FileService files) {
    this.files = files;
  }

  private static ResponseStatusException unauth() {
    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization required");
  }

  /*@GetMapping("{id}")
  public String serveDownloadPage(@PathVariable String id) {

  }*/

  @GetMapping("info/{id}")
  public FileInfo getInfo(@PathVariable String id) {
    return files.getInfo(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File does not exist"));
  }

  /*@GetMapping("dl/{id}")
  public EntityResponse<Resource> download(@PathVariable String id, @RequestHeader Map<String, String> headers) {
    String auth = headers.get("Authorization");
    if (auth == null) {
      throw unauth();
    }
    String[] tokens = auth.split(" ");
    if (tokens.length != 2 || tokens[0].equals("Basic")) {
      throw unauth();
    }
    byte[] pwdBytes = Base64.getDecoder().decode(tokens[1]);

  }*/

}
