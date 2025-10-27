package com.common.web;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class PageResponse<T> {
    private final List<T> items;
    private final int page;       // 1-based
    private final int size;
    private final long total;
    private final int totalPages;
    private final boolean hasPrev;
    private final boolean hasNext;

    private PageResponse(List<T> items, int page, int size, long total) {
        this.items = Collections.unmodifiableList(Objects.requireNonNullElse(items, List.of()));
        this.page = Math.max(1, page);
        this.size = Math.max(1, size);
        this.total = Math.max(0, total);
        this.totalPages = (int) Math.max(1, Math.ceil(this.total / (double) this.size));
        this.hasPrev = this.page > 1;
        this.hasNext = this.page < this.totalPages;
    }

    public static <T> PageResponse<T> of(List<T> items, int page, int size, long total) {
        return new PageResponse<>(items, page, size, total);
    }

    public List<T> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
    public int getTotalPages() { return totalPages; }
    public boolean isHasPrev() { return hasPrev; }
    public boolean isHasNext() { return hasNext; }
}
