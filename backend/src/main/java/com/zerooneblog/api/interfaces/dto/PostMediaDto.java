package com.zerooneblog.api.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostMediaDto {
    private Long id;
    private String mediaUrl;
    private String mediaType; 
    private Integer displayOrder;
    private LocalDateTime createdAt;
}