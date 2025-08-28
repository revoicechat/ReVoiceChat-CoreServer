package fr.revoicechat.core.repository.page;

import java.util.List;

public record PageResult<T>(List<T> content,
                            int pageNumber,
                            int pageSize,
                            long totalElements,
                            long totalPages) {

  public PageResult(final List<T> content, final int pageNumber, final int pageSize, final long totalElements) {
    this(content, pageNumber, pageSize, totalElements, getTotalPages(pageSize, totalElements));
  }

  private static long getTotalPages(int pageSize, long totalElements) {
    return (long) Math.ceil((double) totalElements / pageSize);
  }
}
