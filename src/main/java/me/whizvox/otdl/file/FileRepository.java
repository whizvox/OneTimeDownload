package me.whizvox.otdl.file;

import org.springframework.data.repository.CrudRepository;

public interface FileRepository extends CrudRepository<FileInfo, String> {
}
