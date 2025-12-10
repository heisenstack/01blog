package com.zerooneblog.api.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

import java.util.Set;
import java.util.HashSet;
import java.time.Instant;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private Role role = Role.USER;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Post> posts = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "user_followers", joinColumns = @JoinColumn(name = "follower_id"), inverseJoinColumns = @JoinColumn(name = "following_id"))
    @JsonIgnore
    private Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "following")
    @JsonIgnore
    private Set<User> followers = new HashSet<>();

    @Column
    private Long reportedCount = 0L;

    @Column
    private Long reportingCount = 0L;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}