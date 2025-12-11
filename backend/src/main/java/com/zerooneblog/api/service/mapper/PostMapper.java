package com.zerooneblog.api.service.mapper;

import com.zerooneblog.api.domain.Post;
import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.persistence.PostLikeRepository;
import com.zerooneblog.api.interfaces.dto.PostMediaDto;
import com.zerooneblog.api.interfaces.dto.PostResponse;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    private final PostLikeRepository postLikeRepository;
    private final PostMediaMapper postMediaMapper;

    public PostMapper(PostLikeRepository postLikeRepository, PostMediaMapper postMediaMapper) {
        this.postLikeRepository = postLikeRepository;
        this.postMediaMapper = postMediaMapper;
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
        dto.setHidden(false);
        dto.setReportedCount(post.getReportedCount() != null ? post.getReportedCount() : 0L);
        List<PostMediaDto> postMediaDto = post.getMediaFoLES().stream()
        .map(mediafile -> 
            postMediaMapper.toDto(mediafile)
        ).collect(Collectors.toList());
        dto.setMediaFiles(postMediaDto);
        
        return dto;
    }
}