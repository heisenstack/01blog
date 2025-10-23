package com.zerooneblog.api.interfaces.dto;


public class PostAuthorResponse {
    private String username;

    // Constructor
    public PostAuthorResponse(Long id, String username) {
        this.username = username;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }
}
