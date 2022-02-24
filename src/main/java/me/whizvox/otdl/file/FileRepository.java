package me.whizvox.otdl.file;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FileRepository extends CrudRepository<FileInfo, String> {

  @Query("SELECT info FROM FileInfo info WHERE info.expires < current_timestamp")
  List<FileInfo> findAllExpired();

}
