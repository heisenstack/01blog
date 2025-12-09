package com.zerooneblog.api.domain;

import lombok.Data;
import jakarta.persistence.*;
import java.time.Instant;

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

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
