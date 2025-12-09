package com.zerooneblog.api.service;

import com.zerooneblog.api.domain.Post;
import com.zerooneblog.api.domain.User;

import com.zerooneblog.api.infrastructure.persistence.*;
import com.zerooneblog.api.interfaces.dto.PostDTO;
import com.zerooneblog.api.interfaces.dto.PostResponse;
import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;
import com.zerooneblog.api.interfaces.exception.UnauthorizedActionException;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository, PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
    }

    @Transactional
    public PostResponse createPost(PostDTO request, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(author);
        Post savedPost = postRepository.save(post);

        return mapToDto(savedPost,author);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts(Authentication authentication) {
        User currentUser = getCurrentUserFromAuthentication(authentication);
        List<Post> posts = postRepository.findAll();
        return posts.stream()
        .map(post -> mapToDto(post, currentUser))
        .collect(Collectors.toList());
    }

    public PostResponse getPostById(Long postId, Authentication authentication) {
        User currentUser = getCurrentUserFromAuthentication(authentication);
        Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        return mapToDto(post, currentUser);
    }

    @Transactional
    public Post updatePost(Long postId, PostDTO request, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new UnauthorizedActionException(
                    "User " + username + " is not authorized to update post " + postId);
        }
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        return postRepository.save(post);
    }

    public String deletePost(Long id, String username) {
        Post post = postRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        if (!post.getAuthor().getUsername().equals(username)) {
            throw new UnauthorizedActionException(String.format("User '%s' is not authorized to delete post %d", username, id));
        }
        postRepository.delete(post);
        return "Post " + id + " has been deleted successfully!";
    }

    private PostResponse mapToDto(Post post, User currentUser) {
        PostResponse dto = new PostResponse();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setAuthorId(post.getAuthor().getId());
        dto.setAuthorUsername(post.getAuthor().getUsername());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setLikeCount(postLikeRepository.countByPostId(post.getId()));
        dto.setLikedByCurrentUser(currentUser != null && 
            postLikeRepository.existsByUserIdAndPostId(post.getId(), currentUser.getId())
        );
        return dto;
    }
    private User getCurrentUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())){
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
}
