package com.zerooneblog.api.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zerooneblog.api.domain.PostLike;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    long countByPostId(Long postId);
    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);
}
