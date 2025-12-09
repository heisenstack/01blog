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
    private final UserService userService;

    public PostService(PostRepository postRepository, UserRepository userRepository, PostLikeRepository postLikeRepository, UserService userService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
        this.userService = userService;
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
        User currentUser = userService.getCurrentUserFromAuthentication(authentication);
        List<Post> posts = postRepository.findAll();
        return posts.stream()
        .map(post -> mapToDto(post, currentUser))
        .collect(Collectors.toList());
    }

    public PostResponse getPostById(Long postId, Authentication authentication) {
        User currentUser = userService.getCurrentUserFromAuthentication(authentication);
        Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        
        System.out.println(post.getReportedCount());
        return mapToDto(post, currentUser);
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostDTO request, Authentication authentication) {
        User currentUser = userService.getCurrentUserFromAuthentication(authentication);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        if (!post.getAuthor().getUsername().equals(currentUser.getUsername())) {
            throw new UnauthorizedActionException(
                    "User " + currentUser.getUsername() + " is not authorized to update post " + postId);
        }
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        Post savedPost = postRepository.save(post);
        return mapToDto(savedPost, currentUser);
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
        if (post.getReportedCount() == null) {
            dto.setReportedCount(0L);
        }else {
            dto.setReportedCount(post.getReportedCount());
        }
        return dto;
    }
}
