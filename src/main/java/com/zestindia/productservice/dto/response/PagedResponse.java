package com.zestindia.productservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated response wrapper for collections")
public class PagedResponse<T> {

    @Schema(description = "List of items in current page")
    private List<T> content;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int pageNo;

    @Schema(description = "Page size (number of items per page)", example = "10")
    private int pageSize;

    @Schema(description = "Total count of elements across all pages", example = "42")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;

    public PagedResponse() {
    }

    public PagedResponse(List<T> content, int pageNo, int pageSize, long totalElements, int totalPages, boolean last) {
        this.content = content;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}
