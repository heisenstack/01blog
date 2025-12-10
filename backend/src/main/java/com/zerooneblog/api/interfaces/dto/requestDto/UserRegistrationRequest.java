package com.zerooneblog.api.interfaces.dto.requestDto;


import lombok.Data;

@Data
public class UserRegistrationRequest {
    public String username;
    private String email;
    private String password;
    private String FullName;

}
