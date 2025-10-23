package com.zerooneblog.api.interfaces.dto;

import java.time.Instant;

public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private Instant createdAt;
    private PostAuthorResponse author;

    // Constructor
    public PostResponse(Long id, String title, String content, Instant createdAt, PostAuthorResponse author) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.author = author;
    }


    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
    public PostAuthorResponse getAuthor() { return author; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setAuthor(PostAuthorResponse author) { this.author = author; }
}
