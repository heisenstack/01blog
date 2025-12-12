package com.zerooneblog.api.interfaces.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PostsResponseDto {
    private List<PostResponse> posts;
    private int pageNumber;
    private long pageSize;
    private int totalPages;
    private long totalElements;
    private boolean last;

     public PostsResponseDto(List<PostResponse> posts, int pageNumber, int pageSize, long totalElements, int totalPages, boolean last) {
        this.posts = posts;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }
}
