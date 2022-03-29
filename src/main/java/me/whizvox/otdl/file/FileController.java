package me.whizvox.otdl.file;

import me.whizvox.otdl.exception.InvalidLifespanException;
import me.whizvox.otdl.exception.NoFileException;
import me.whizvox.otdl.exception.WrongPasswordException;
import me.whizvox.otdl.security.SecurityService;
import me.whizvox.otdl.user.User;
import me.whizvox.otdl.util.ApiResponse;
import me.whizvox.otdl.util.PagedResponseData;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LessThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
    CharBuffer pwdBuffer;
    try {
      pwdBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(Base64.getUrlDecoder().decode(password)));
    } catch (IllegalArgumentException e) {
      return null;
    }
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
    if (pwdArr != null) {
      try {
        Optional<FileInfo> infoOp = files.getInfo(id);
        if (infoOp.isPresent()) {
          FileInfo info = infoOp.get();
          if (security.authenticate(info.getAuthToken(), pwdArr)) {
            return ApiResponse.ok(new PublicFileInfo(info));
          }
        }
      } catch(Exception e){
        LOG.error("Unexpected exception thrown while fetching file info: " + id, e);
      } finally {
        Arrays.fill(pwdArr, '\u0000');
      }
    }
    // fake not found response
    return ApiResponse.notFound(id);
  }

  @GetMapping("dl/{id}")
  public ResponseEntity<Object> download(@PathVariable String id, @RequestParam(required = false) String password) {
    if (password == null) {
      return ApiResponse.unauthorized();
    }
    char[] pwdArr = decodePassword(password);
    if (pwdArr != null) {
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
      }
    }
    return ApiResponse.notFound(id);
  }

  @GetMapping("available/{id}")
  public ResponseEntity<Object> checkIfAvailable(@PathVariable String id, @RequestParam(required = false) String password) {
    if (password == null) {
      return ApiResponse.unauthorized();
    }
    char[] pwdArr = decodePassword(password);
    if (pwdArr != null) {
      try {
        Optional<FileInfo> infoOp = files.getInfo(id);
        if (infoOp.isPresent()) {
          FileInfo info = infoOp.get();
          if (!info.isDownloaded() && security.authenticate(info.getAuthToken(), pwdArr)) {
            return ApiResponse.ok(true);
          }
        }
      } catch (Exception e) {
        LOG.error("Unexpected error while checking file availability", e);
      } finally {
        Arrays.fill(pwdArr, '\u0000');
      }
    }
    return ApiResponse.ok(false);
  }

  @PostMapping
  public ResponseEntity<Object> upload(@RequestParam(required = false) MultipartFile file,
                                       @RequestParam(required = false) String password,
                                       @RequestParam(defaultValue = "60") int lifespan,
                                       @AuthenticationPrincipal User user) {
    if (password == null) {
      return ApiResponse.badRequest("Missing password parameter");
    }
    int maxLifespanAllowed = 0;
    long maxSizeAllowed = 0;
    if (user == null) {
      maxLifespanAllowed = config.getMaxLifespanAnonymous();
      maxSizeAllowed = config.getMaxFileSizeAnonymous();
    } else {
      switch (user.getGroup()) {
        case USER -> {
          switch (user.getRank()) {
            case ANONYMOUS -> {
              maxLifespanAllowed = config.getMaxLifespanAnonymous();
              maxSizeAllowed = config.getMaxFileSizeAnonymous();
            }
            case MEMBER -> {
              maxLifespanAllowed = config.getMaxLifespanMember();
              maxSizeAllowed = config.getMaxFileSizeMember();
            }
            case CONTRIBUTOR -> {
              maxLifespanAllowed = config.getMaxLifespanContributor();
              maxSizeAllowed = config.getMaxFileSizeContributor();
            }
          }
        }
        case ADMIN -> {
          maxLifespanAllowed = Integer.MAX_VALUE;
          maxSizeAllowed = Long.MAX_VALUE;
        }
      }
    }
    if (maxSizeAllowed == 0) {
      return ApiResponse.forbidden("User is restricted, cannot upload files");
    }
    if (file == null) {
      return ApiResponse.badRequest("Missing file");
    }
    if (lifespan > maxLifespanAllowed) {
      return ApiResponse.badRequest("Lifespan too large, max " + maxLifespanAllowed + " min");
    }
    if (file.getSize() > maxSizeAllowed) {
      return ApiResponse.badRequest("File size too large, max " + maxSizeAllowed + " B");
    }

    char[] pwdArr = decodePassword(password);
    if (pwdArr != null) {
      try {
        return ApiResponse.ok(new PublicFileInfo(files.upload(file, lifespan, pwdArr)));
      } catch (InvalidLifespanException | NoFileException e) {
        return ApiResponse.badRequest(e.getMessage());
      } catch (Exception e) {
        LOG.error("Could not upload file", e);
        throw internalServerError(null, e);
      } finally {
        Arrays.fill(pwdArr, '\u0000');
      }
    }
    return ApiResponse.badRequest("Invalid password base64 string");
  }

  @DeleteMapping("{id}")
  public ResponseEntity<Object> delete(@PathVariable String id, @RequestParam(required = false) String password) {
    if (password == null) {
      return ApiResponse.unauthorized();
    }
    char[] pwdArr = decodePassword(password);
    if (pwdArr != null) {
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
      }
    }
    // intentionally return false positives in the event of an unknown ID or wrong password
    return ApiResponse.ok();
  }

  @GetMapping("/search")
  public ResponseEntity<Object> search(
      @And({
          @Spec(params = "fileName", path = "fileName", spec = LikeIgnoreCase.class),
          @Spec(params = "uploadedAfter", path = "uploaded", spec = GreaterThanOrEqual.class),
          @Spec(params = "uploadedBefore", path = "uploaded", spec = LessThanOrEqual.class),
          @Spec(params = "minSize", path = "originalSize", spec = GreaterThanOrEqual.class),
          @Spec(params = "maxSize", path = "originalSize", spec = LessThanOrEqual.class),
          @Spec(params = "downloaded", path = "downloaded", spec = Equal.class)
      }) Specification<FileInfo> spec,
      Pageable pageable) {
    if (pageable.getSort() == Sort.unsorted()) {
      pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.DESC, "uploaded");
    }
    return ApiResponse.ok(new PagedResponseData<>(files.search(spec, pageable)));
  }

}
