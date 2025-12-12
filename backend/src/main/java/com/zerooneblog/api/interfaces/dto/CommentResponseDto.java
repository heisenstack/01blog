package com.zerooneblog.api.interfaces.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentResponseDto {
    private List<CommentDTO> content;
    private int pageNumber;
    private long pageSize;
    private int totalPages;
    private long totalElements;
    private boolean last;

        public CommentResponseDto(List<CommentDTO> content, int pageNumber, int pageSize, long totalElements, int totalPages, boolean last) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }
}
