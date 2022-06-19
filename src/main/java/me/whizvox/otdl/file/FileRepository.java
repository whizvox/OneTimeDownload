package me.whizvox.otdl.file;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends PagingAndSortingRepository<FileInfo, String>, JpaSpecificationExecutor<FileInfo> {

  @Query("SELECT info FROM FileInfo info WHERE info.expires < current_timestamp")
  List<FileInfo> findAllExpired();

  @Query("SELECT SUM(storedSize) FROM FileInfo")
  Long getStorageUsed();

  @Query("SELECT file FROM FileInfo file WHERE file.user IS NOT NULL AND file.user.id = :userId")
  Page<FileInfo> findAllFilesUploadedBy(UUID userId, Pageable pageable);

  @Query("SELECT file FROM FileInfo file WHERE file.id = :fileId AND file.user IS NOT NULL AND file.user.id = :userId")
  Optional<FileInfo> findFileUploadedBy(String fileId, UUID userId);

  @Modifying
  @Transactional
  @Query("UPDATE FileInfo file SET file.user = null WHERE file.user IS NOT NULL AND file.user.id = :userId")
  int clearUser(UUID userId);

}
