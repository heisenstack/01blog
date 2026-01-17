package com.zerooneblog.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;

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
        private final PostService postService;

        public PostLikeService(PostLikeRepository postLikeRepository, PostRepository postRepository,
                        UserRepository userRepository, NotificationService notificationService,
                        PostService postService) {
                this.postLikeRepository = postLikeRepository;
                this.postRepository = postRepository;
                this.userRepository = userRepository;
                this.notificationService = notificationService;
                this.postService = postService;
        }

        @Transactional
        public PostLikeResponseDto likePost(Long postId, Authentication authentication) {
                postService.validatePostAccess(postId, authentication);

                String username = authentication.getName();
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

                Post post = postRepository.findById(postId)
                                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

                if (postLikeRepository.existsByUserIdAndPostId(user.getId(), post.getId())) {
                        throw new IllegalStateException("Post is already liked.");
                }

                try {
                        PostLike newLike = new PostLike();
                        newLike.setUser(user);
                        newLike.setPost(post);
                        postLikeRepository.save(newLike);

                        User postAuthor = post.getAuthor();
                        String message = user.getUsername() + " liked your post: \"" + post.getTitle() + "\"";
                        notificationService.createNotification(
                                        postAuthor,
                                        user,
                                        Notification.NotificationType.NEW_LIKE,
                                        message,
                                        post);

                } catch (DataIntegrityViolationException e) {
                        throw new IllegalStateException("Post is already liked.");
                }

                long updatedLikeCount = postLikeRepository.countByPostId(postId);
                return new PostLikeResponseDto(updatedLikeCount, true);
        }

        @Transactional
        public PostLikeResponseDto unlikePost(Long postId, Authentication authentication) {
                postService.validatePostAccess(postId, authentication);

                String username = authentication.getName();
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

                Post post = postRepository.findById(postId)
                                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

                if (!postLikeRepository.existsByUserIdAndPostId(user.getId(), post.getId())) {
                        throw new IllegalStateException("Post is not liked.");
                }

                postLikeRepository.findByUserIdAndPostId(user.getId(), post.getId())
                                .ifPresent(like -> postLikeRepository.delete(like));

                long updatedLikeCount = postLikeRepository.countByPostId(postId);
                return new PostLikeResponseDto(updatedLikeCount, false);
        }       

}
