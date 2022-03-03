package me.whizvox.otdl.file;

import me.whizvox.otdl.exception.InvalidLifespanException;
import me.whizvox.otdl.exception.NoFileException;
import me.whizvox.otdl.exception.WrongPasswordException;
import me.whizvox.otdl.security.SecurityService;
import me.whizvox.otdl.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@RestController
@RequestMapping("files")
public class FileController {

  private static final Logger LOG = LoggerFactory.getLogger(FileController.class);

  private FileService files;
  private SecurityService security;
  private final FileConfiguration config;

  @Autowired
  public FileController(FileService files, SecurityService security, FileConfiguration config) {
    this.files = files;
    this.security = security;
    this.config = config;
  }

  private static char[] decodePassword(String password) {
    CharBuffer pwdBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(Base64.getUrlDecoder().decode(password)));
    char[] pwdArr = new char[pwdBuffer.length()];
    pwdBuffer.get(pwdArr);
    pwdBuffer.flip();
    for (int i = 0; i < pwdArr.length; i++) {
      pwdBuffer.put('\u0000');
    }
    pwdBuffer.clear();
    return pwdArr;
  }

  private static ResponseStatusException internalServerError(String msg, Exception e) {
    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, msg, e);
  }

  @GetMapping("info/{id}")
  public ResponseEntity<Object> getInfo(@PathVariable String id, @RequestParam(required = false) String password) {
    if (password == null) {
      return ApiResponse.unauthorized();
    }
    char[] pwdArr = decodePassword(password);
    try {
      Optional<FileInfo> infoOp = files.getInfo(id);
      if (infoOp.isPresent()) {
        FileInfo info = infoOp.get();
        if (security.authenticate(info.getAuthToken(), pwdArr)) {
          return ApiResponse.ok(new PublicFileInfo(info));
        }
      }
    } catch (Exception e) {
      LOG.error("Unexpected exception thrown while fetching file info: " + id, e);
    } finally {
      Arrays.fill(pwdArr, '\u0000');
    }
    // fake not found response
    return ApiResponse.notFound(id);
  }

  @GetMapping("dl/{id}")
  public ResponseEntity<Object> download(@PathVariable String id, @RequestParam(required = false) String password) {
    if (password == null) {
      return ApiResponse.unauthorized();
    }
    CharBuffer pwdBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(Base64.getUrlDecoder().decode(password)));
    char[] pwdArr = new char[pwdBuffer.length()];
    pwdBuffer.get(pwdArr);
    try {
      Resource res = files.serve(id, pwdArr, true);
      if (res != null) {
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + res.getFilename() + "\"")
            .body(res);
      }
    } catch (WrongPasswordException e) {
      return ApiResponse.unauthorized();
    } catch (Exception e) {
      LOG.error("Unexpected exception attempting to download file: " + id, e);
    } finally {
      Arrays.fill(pwdArr, '\u0000');
      pwdBuffer.flip();
      pwdBuffer.put(new char[pwdArr.length]);
      pwdBuffer.clear();
    }
    return ApiResponse.notFound(id);
  }

  @PostMapping
  public ResponseEntity<Object> upload(@RequestParam(required = false) MultipartFile file, @RequestParam(required = false) String password, @RequestParam(defaultValue = "60") int lifespan) {
    if (password == null) {
      return ApiResponse.badRequest("Missing password parameter");
    }
    // TODO Integrate member file restrictions
    if (file != null && file.getSize() > config.getMaxFileSizeAnonymous()) {
      return ApiResponse.badRequest("File too big, max " + config.getMaxFileSizeAnonymous() + " B");
    }
    CharBuffer pwdBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(Base64.getUrlDecoder().decode(password)));
    char[] pwdArr = new char[pwdBuffer.length()];
    pwdBuffer.get(pwdArr);
    try {
      return ApiResponse.ok(new PublicFileInfo(files.upload(file, lifespan, pwdArr)));
    } catch (InvalidLifespanException | NoFileException e) {
      return ApiResponse.badRequest(e.getMessage());
    } catch (IOException e) {
      throw internalServerError("Could not upload file", e);
    } catch (Exception e) {
      throw internalServerError(null, e);
    } finally {
      Arrays.fill(pwdArr, '\u0000');
      pwdBuffer.flip();
      pwdBuffer.put(pwdArr);
      pwdBuffer.clear();
    }
  }

  @DeleteMapping("{id}")
  public ResponseEntity<Object> delete(@PathVariable String id, @RequestParam(required = false) String password) {
    if (password == null) {
      return ApiResponse.unauthorized();
    }
    CharBuffer pwdBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(Base64.getUrlDecoder().decode(password)));
    char[] pwdArr = new char[pwdBuffer.length()];
    pwdBuffer.get(pwdArr);
    try {
      Optional<FileInfo> infoOp = files.getInfo(id);
      if (infoOp.isPresent()) {
        FileInfo info = infoOp.get();
        if (security.authenticate(info.getAuthToken(), pwdArr)) {
          files.delete(id);
        }
      }
    } catch (WrongPasswordException ignored) {
    } catch (Exception e) {
      LOG.error("Unexpected error attempting to delete file", e);
    } finally {
      Arrays.fill(pwdArr, '\u0000');
      pwdBuffer.flip();
      pwdBuffer.put(pwdArr);
      pwdBuffer.clear();
    }
    // intentionally return false positives in the event of an unknown ID or wrong password
    return ApiResponse.ok();
  }

}
