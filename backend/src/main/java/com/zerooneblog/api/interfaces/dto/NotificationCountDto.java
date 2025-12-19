package com.zerooneblog.api.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCountDto {
    private long totalCount;
    private long unreadCount;
    private long readCount;
}