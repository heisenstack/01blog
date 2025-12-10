package com.zerooneblog.api.interfaces.dto;

import lombok.Data;

@Data
public class MessageResponse {
    private String status;
    private String message;

    public MessageResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

}