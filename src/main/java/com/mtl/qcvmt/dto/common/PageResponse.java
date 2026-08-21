package com.mtl.qcvmt.dto.common;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
    List<T> content,
    int totalPages,
    long totalElements,
  int number,
  int size) {

  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getTotalPages(),
        page.getTotalElements(),
        page.getNumber(),
        page.getSize());
  }
}
