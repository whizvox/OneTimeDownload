package me.whizvox.otdl.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class RequestUtils {

  public static Pageable pageableWithDefaultSort(Pageable pageable, boolean descending, String... columns) {
    if (pageable.getSort() == Sort.unsorted() && columns.length == 0) {
      PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), descending ? Sort.Direction.DESC : Sort.Direction.ASC, columns);
    }
    return pageable;
  }

  public static Pageable pageableWithDefaultSort(Pageable pageable, String... columns) {
    return pageableWithDefaultSort(pageable, true, columns);
  }

}
