package com.zerooneblog.api.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore; 
import java.util.List; 
import java.util.ArrayList;
import lombok.Data;

@Data
@Entity
@Table(name= "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String username;
    private String email;
    private String password;
    private String role; 
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore 
    private List<Post> posts = new ArrayList<>();

    // Constructor
    public User() {}

    public User(String username, String email, String password) {
        this(username, email, password, "USER");
    }
    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
