package me.whizvox.otdl.file;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface FileRepository extends PagingAndSortingRepository<FileInfo, String>, JpaSpecificationExecutor<FileInfo> {

  @Query("SELECT info FROM FileInfo info WHERE info.expires < current_timestamp")
  List<FileInfo> findAllExpired();

  @Query("SELECT SUM(storedSize) FROM FileInfo")
  Long getStorageUsed();

}
