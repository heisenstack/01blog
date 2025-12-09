package com.zerooneblog.api.service;

import org.springframework.stereotype.Service;

import com.zerooneblog.api.domain.Post;
import com.zerooneblog.api.domain.PostLike;
import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.persistence.PostLikeRepository;
import com.zerooneblog.api.infrastructure.persistence.PostRepository;
import com.zerooneblog.api.infrastructure.persistence.UserRepository;
import com.zerooneblog.api.interfaces.dto.PostLikeResponseDto;
import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;

@Service
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostLikeService(PostLikeRepository postLikeRepository, PostRepository postRepository,
            UserRepository userRepository) {
        this.postLikeRepository = postLikeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public PostLikeResponseDto likePost(Long postId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        if (!postLikeRepository.existsByUserIdAndPostId(user.getId(), post.getId())) {
            PostLike newLike = new PostLike();
            newLike.setUser(user);
            newLike.setPost(post);
            postLikeRepository.save(newLike);
        }
        long updatedLikeCount = postLikeRepository.countByPostId(postId);
        return new PostLikeResponseDto(updatedLikeCount, true);
    }

    public PostLikeResponseDto unlikePost(Long postId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        postLikeRepository.findByUserIdAndPostId(user.getId(), post.getId())
                .ifPresent(like -> {
                    postLikeRepository.delete(like);
                });

        long updatedLikeCount = postLikeRepository.countByPostId(postId);
        return new PostLikeResponseDto(updatedLikeCount, true);
    }

}
