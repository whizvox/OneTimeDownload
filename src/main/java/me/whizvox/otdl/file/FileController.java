package me.whizvox.otdl.file;

import me.whizvox.otdl.exception.InvalidLifespanException;
import me.whizvox.otdl.exception.NoFileException;
import me.whizvox.otdl.exception.UnknownIdException;
import me.whizvox.otdl.exception.WrongPasswordException;
import me.whizvox.otdl.security.SecurityService;
import me.whizvox.otdl.storage.StorageException;
import me.whizvox.otdl.user.User;
import me.whizvox.otdl.user.UserRole;
import me.whizvox.otdl.util.ApiResponse;
import me.whizvox.otdl.util.PagedResponseData;
import me.whizvox.otdl.util.RequestUtils;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
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
                                       @RequestParam(defaultValue = "30") int lifespan,
                                       @AuthenticationPrincipal User user) {
    if (user != null) {
      if (user.getRole() == UserRole.RESTRICTED) {
        return ApiResponse.forbidden("Account is restricted");
      } else if (!user.isVerified()) {
        return ApiResponse.forbidden("Account email is unverified");
      }
    }
    if (file == null) {
      return ApiResponse.badRequest("Missing file");
    }
    if (password == null) {
      return ApiResponse.badRequest("Missing password");
    }
    int maxLifespan = config.getMaxLifespan(user);
    long maxFileSize = config.getMaxFileSize(user);
    if (lifespan > maxLifespan || lifespan < 1) {
      return ApiResponse.badRequest("Bad lifespan (%d): min 1, max %d minutes".formatted(lifespan, maxLifespan));
    }
    if (file.getSize() > maxFileSize) {
      return ApiResponse.badRequest("Bad file size (%d): max %d bytes".formatted(file.getSize(), maxFileSize));
    }

    char[] pwdArr = decodePassword(password);
    if (pwdArr != null) {
      try {
        return ApiResponse.ok(new PublicFileInfo(files.upload(file, lifespan, pwdArr, user)));
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

  @GetMapping("search")
  @PreAuthorize("@authorizationService.hasPermission(principal, 'ADMIN')")
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

  @PutMapping("{id}")
  public ResponseEntity<Object> update(
      @PathVariable String id,
      @RequestParam(required = false) String fileName,
      // TODO Figure out how to properly set date time format globally
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expires,
      @RequestParam(required = false) Boolean downloaded,
      @AuthenticationPrincipal User user) {
    if (user == null) {
      return ApiResponse.forbidden("Cannot modify file as anonymous user");
    }
    if (user.getRole() == UserRole.RESTRICTED) {
      return ApiResponse.forbidden("Account is restricted");
    }
    Optional<FileInfo> optional;
    if (user.getRole() == UserRole.ADMIN) {
      optional = files.getInfo(id);
    } else {
      optional = files.getFileUploadedByUser(id, user.getId());
    }
    return optional.map(file -> {
      if (fileName != null) {
        if (fileName.isEmpty()) {
          return ApiResponse.badRequest("Bad file name, cannot be empty");
        }
        file.setFileName(fileName);
      }
      if (expires != null) {
        Duration duration = Duration.between(file.getUploaded(), expires);
        int maxLifespan = config.getMaxLifespan(user);
        if (duration.toMinutes() > maxLifespan) {
          return ApiResponse.badRequest("Lifespan too long (%d), %d minutes max".formatted(duration.toMinutes(), maxLifespan));
        }
        file.setExpires(expires);
      }
      if (downloaded != null) {
        if (user.getRole() != UserRole.ADMIN) {
          return ApiResponse.badRequest("Do not have sufficient permissions to set downloaded flag");
        }
        file.setDownloaded(downloaded);
      }
      try {
        files.update(file);
        return ApiResponse.ok();
      } catch (UnknownIdException e) {
        return ApiResponse.notFound(id);
      }
    }).orElse(ApiResponse.notFound(id));
  }

  @GetMapping("all")
  public ResponseEntity<Object> getFilesUploadedBySelf(@AuthenticationPrincipal User user,
                                                       Pageable pageable) {
    if (user == null) {
      return ApiResponse.unauthorized();
    }
    return ApiResponse.ok(new PagedResponseData<>(
          files.getFilesUploadedByUser(
              user.getId(),
              RequestUtils.pageableWithDefaultSort(pageable, "uploaded")
          ).map(PublicFileInfo::new))
    );
  }

  @PostMapping("/delete")
  @PreAuthorize("@authorizationService.hasPermission(principal, 'ADMIN')")
  public ResponseEntity<Object> deleteBulk(@RequestParam String[] ids) {
    if (ids != null && ids.length > 0) {
      try {
        files.delete(List.of(ids));
      } catch (StorageException e) {
        LOG.error("Could not bulk delete files", e);
        return ApiResponse.internalServerError("Could not bulk delete files");
      }
    }
    return ApiResponse.ok();
  }

}
