package com.zerooneblog.api.interfaces.dto;

public class UserRegistrationRequest {
    public String username;
    private String email;
    private String password;

    // Getters
    public String GetUsername() { return username; }
    public String GetEmail() { return email; }
    public String getPassword() { return password; }

    // Setters
    public void SetUsername(String username) { this.username = username;}
    public void SetEmail(String email) { this.email = email;}
    public void SetPassword(String password) { this.password = password;}
}
