package com.zerooneblog.api.domain;

import lombok.Data;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

@Data
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

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
}
