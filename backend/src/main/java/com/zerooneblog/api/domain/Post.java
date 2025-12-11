package com.zerooneblog.api.domain;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@Entity
@Table(name = "posts")
public class Post {
    public enum MediaType {
        IMAGE,
        VIDEO,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    @Column(nullable = false)
    private String title;

    @Lob
    @NotBlank(message = "Content is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PostMedia> mediaFoLES = new ArrayList<>();

    @Column(name = "media_url")
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    private MediaType mediaType;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();

    @Column
    private Long reportedCount = 0L;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
      @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post)) return false;
        Post post = (Post) o;
        return id != null && id.equals(post.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
