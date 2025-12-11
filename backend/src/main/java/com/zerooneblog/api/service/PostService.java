package com.zerooneblog.api.service;

import com.zerooneblog.api.domain.Post;
import com.zerooneblog.api.domain.PostMedia;
import com.zerooneblog.api.domain.User;

import com.zerooneblog.api.infrastructure.persistence.*;
import com.zerooneblog.api.interfaces.dto.PostDTO;
import com.zerooneblog.api.interfaces.dto.PostResponse;
import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;
import com.zerooneblog.api.interfaces.exception.UnauthorizedActionException;
import com.zerooneblog.api.service.mapper.PostMapper;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    private static final int MAX_MEDIA_FILES = 5;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PostMapper postMapper;
    private final FileStorageService fileStorageService;
    private final PostMediaRepository postMediaRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository, UserService userService, PostMapper postMapper, FileStorageService fileStorageService, PostMediaRepository postMediaRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.postMapper = postMapper;
        this.fileStorageService = fileStorageService;
        this.postMediaRepository = postMediaRepository;
    }

    @Transactional
    public PostResponse createPost(PostDTO request, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        int fileCount = (request.getMediaFiles() != null) ? request.getMediaFiles().length : 0;
        if (fileCount > MAX_MEDIA_FILES) {
            throw new IllegalArgumentException("Cannot upload more than " + MAX_MEDIA_FILES + " media files.");
        }
        
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(author);
        post.setHidden(false);

        Post savedPost = postRepository.save(post);
        if (request.getMediaFiles() != null && request.getMediaFiles().length > 0) {
            int order = 0;
            for (MultipartFile mediaFile: request.getMediaFiles()) {
                PostMedia postMedia = processAndSaveMedia(mediaFile, savedPost, order);
                if (postMedia != null)    {
                    order++;
                }
            }
        }

        return postMapper.toDto(savedPost,author);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts(Authentication authentication) {
        User currentUser = userService.getCurrentUserFromAuthentication(authentication);
        List<Post> posts = postRepository.findAll();
        return posts.stream()
        .map(post -> postMapper.toDto(post, currentUser))
        .collect(Collectors.toList());
    }

    public PostResponse getPostById(Long postId, Authentication authentication) {
        User currentUser = userService.getCurrentUserFromAuthentication(authentication);
        Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        System.out.println(post.getReportedCount());
        return postMapper.toDto(post, currentUser);
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
        return postMapper.toDto(savedPost, currentUser);
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

    private PostMedia processAndSaveMedia(MultipartFile mediaFile, Post post, int order) {
        String contentType = mediaFile.getContentType();
        PostMedia.MediaType mediaType = null;

        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                mediaType = PostMedia.MediaType.IMAGE;
            }else if(contentType.startsWith("video/")) {
                mediaType = PostMedia.MediaType.VIDEO;
            }
        }
        if (mediaType != null) {
            String fileName = fileStorageService.storeFile(mediaFile);
            PostMedia postMedia = new PostMedia();
            postMedia.setPost(post);
            postMedia.setMediaUrl(fileName);
            postMedia.setMediaType(mediaType);
            postMedia.setDisplayOrder(order);

            PostMedia savedPostMedia = postMediaRepository.save(postMedia);
            post.getMediaFoLES().add(savedPostMedia);
            return savedPostMedia;
        }
        return null;
    }
}
