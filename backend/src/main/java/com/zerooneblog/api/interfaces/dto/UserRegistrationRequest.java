package com.zerooneblog.api.interfaces.dto;

public class UserRegistrationRequest {
    public String username;
    private String email;
    private String password;

    // Getters
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    // Setters
    public void setUsername(String username) { this.username = username;}
    public void setEmail(String email) { this.email = email;}
    public void setPassword(String password) { this.password = password;}
}
