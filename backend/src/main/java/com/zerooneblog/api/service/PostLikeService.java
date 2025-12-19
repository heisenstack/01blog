package com.zerooneblog.api.service;

import org.springframework.stereotype.Service;

import com.zerooneblog.api.domain.*;
import com.zerooneblog.api.infrastructure.persistence.*;
import com.zerooneblog.api.interfaces.dto.PostLikeResponseDto;
import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;

@Service
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public PostLikeService(PostLikeRepository postLikeRepository, PostRepository postRepository,
            UserRepository userRepository, NotificationService notificationService) {
        this.postLikeRepository = postLikeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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
        User postAuthor = post.getAuthor();
        String message = user.getUsername() + " liked your post: \"" + post.getTitle() + "\"";
        notificationService.createNotification(
                postAuthor,
                user,
                Notification.NotificationType.NEW_LIKE,
                message,
                post);
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
        return new PostLikeResponseDto(updatedLikeCount, false);
    }

}
