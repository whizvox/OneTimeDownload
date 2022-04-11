package me.whizvox.otdl.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponseData<T> {

  public PagedResponseData(Page<T> page) {
    this(page.getNumber(), page.getNumberOfElements(), page.getTotalElements(), page.getTotalPages(), page.getContent());
  }

  private int index;
  private int count;
  private long total;
  private int pages;
  private List<T> items;

}
