package com.zerooneblog.api.service.mapper;

import com.zerooneblog.api.domain.Post;
import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.persistence.PostLikeRepository;
import com.zerooneblog.api.interfaces.dto.PostResponse;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    private final PostLikeRepository postLikeRepository;

    public PostMapper(PostLikeRepository postLikeRepository) {
        this.postLikeRepository = postLikeRepository;
    }

    public PostResponse toDto(Post post, User currentUser) {
        PostResponse dto = new PostResponse();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setAuthorId(post.getAuthor().getId());
        dto.setAuthorUsername(post.getAuthor().getUsername());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setLikeCount(postLikeRepository.countByPostId(post.getId()));
        dto.setLikedByCurrentUser(currentUser != null &&
                postLikeRepository.existsByUserIdAndPostId(currentUser.getId(), post.getId())

        );
        dto.setReportedCount(post.getReportedCount() != null ? post.getReportedCount() : 0L);
        return dto;
    }
}