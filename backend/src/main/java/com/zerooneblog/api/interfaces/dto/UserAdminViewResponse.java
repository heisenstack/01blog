package com.zerooneblog.api.interfaces.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserAdminViewResponse {
    private List<UserAdminViewDto> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    // private Long reportsSubmitted; 


}