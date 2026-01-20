package com.zerooneblog.api.service;

import com.zerooneblog.api.domain.*;

import com.zerooneblog.api.infrastructure.persistence.*;
import com.zerooneblog.api.interfaces.dto.*;
import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;
import com.zerooneblog.api.interfaces.exception.UnauthorizedActionException;
import com.zerooneblog.api.service.mapper.PostMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

// Service for managing posts, including CRUD operations and media handling
@Service
public class PostService {
    private static final int MAX_MEDIA_FILES = 5;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PostMapper postMapper;
    private final FileStorageService fileStorageService;
    private final PostMediaRepository postMediaRepository;
    private final NotificationService notificationService;

    public PostService(PostRepository postRepository, UserRepository userRepository, UserService userService,
            PostMapper postMapper, FileStorageService fileStorageService, PostMediaRepository postMediaRepository,
            NotificationService notificationService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.postMapper = postMapper;
        this.fileStorageService = fileStorageService;
        this.postMediaRepository = postMediaRepository;
        this.notificationService = notificationService;
    }

    // Create a new post with optional media files and notify followers
    @Transactional
    public PostResponse createPost(PostDTO request, String username) {
        String title = request.getTitle();
        String content = request.getContent();

        // Validate title and content are not empty
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty or contain only whitespace");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty or contain only whitespace");
        }

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Validate media file count does not exceed limit
        int fileCount = (request.getMediaFiles() != null) ? request.getMediaFiles().length : 0;
        if (fileCount > MAX_MEDIA_FILES) {
            throw new IllegalArgumentException("Cannot upload more than " + MAX_MEDIA_FILES + " media files.");
        }

        // Create and save new post
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(author);
        post.setHidden(false);

        Post savedPost = postRepository.save(post);
        
        // Process and save media files if provided
        if (request.getMediaFiles() != null && request.getMediaFiles().length > 0) {
            int order = 0;
            for (MultipartFile mediaFile : request.getMediaFiles()) {
                PostMedia postMedia = processAndSaveMedia(mediaFile, savedPost, order);
                if (postMedia != null) {
                    order++;
                }
            }
        }

        // Notify all followers about new post
        List<User> followers = userRepository.findFollowersByUserId(author.getId());
        if (!followers.isEmpty()) {
            String message = author.getUsername() + " posted: \"" + request.getTitle() + "\"";
            notificationService.createNotificationsForFollowers(
                    followers,
                    author,
                    Notification.NotificationType.NEW_POST,
                    message,
                    savedPost);
        }
        return postMapper.toDto(savedPost, author);
    }

    // Get all public posts with pagination (excluding hidden posts for non-admins)
    @Transactional(readOnly = true)
    public PostsResponseDto getAllPosts(int page, int size, Authentication authentication) {
        User currentUser = userService.getCurrentUserFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postsPage = postRepository.findAllWithDetails(pageable);
        
        List<PostResponse> postsResponse = postsPage.getContent().stream()
                .map(post -> postMapper.toDto(post, currentUser)).collect(Collectors.toList());
        return new PostsResponseDto(postsResponse, postsPage.getNumber(), postsPage.getSize(),
                postsPage.getTotalElements(), postsPage.getTotalPages(), false);
    }

    // Get all posts for admin (including hidden posts)
    @Transactional(readOnly = true)
    public PostsResponseDto getAllPostsForAdmin(int page, int size, Authentication authentication) {
        User currentUser = userService.getCurrentUserFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postsPage = postRepository.findAll(pageable);
        
        List<PostResponse> postsResponse = postsPage.getContent().stream()
                .map(post -> postMapper.toDto(post, currentUser)).collect(Collectors.toList());
        return new PostsResponseDto(postsResponse, postsPage.getNumber(), postsPage.getSize(),
                postsPage.getTotalElements(), postsPage.getTotalPages(), false);
    }

    // Get personalized feed from followed users
    @Transactional(readOnly = true)
    public PostsResponseDto getFeedForCurrentUser(int page, int size, Authentication authentication) {
        User currentUser = userService.getCurrentUserFromAuthentication(authentication);

        // Get list of users the current user is following
        List<Long> followedUserIds = userRepository.findFollowingIds(currentUser.getId());

        if (followedUserIds.isEmpty()) {
            return new PostsResponseDto();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postsPage = postRepository.findPostsByUserIdIn(followedUserIds, pageable);

        List<PostResponse> postsResponse = postsPage.getContent().stream()
                .map(post -> postMapper.toDto(post, currentUser)).collect(Collectors.toList());
        return new PostsResponseDto(postsResponse, postsPage.getNumber(), postsPage.getSize(),
                postsPage.getTotalElements(), postsPage.getTotalPages(), false);
    }

    // Get a single post by ID (validates access for hidden posts)
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId, Authentication authentication) {
        validatePostAccess(postId, authentication);
        User currentUser = userService.getCurrentUserFromAuthentication(authentication);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        return postMapper.toDto(post, currentUser);
    }

    // Update a post (only by author)
    @Transactional
    public PostResponse updatePost(Long postId, String title, String content, MultipartFile[] mediaFiles,
            Authentication authentication) {
        validatePostAccess(postId, authentication);

        // Validate title and content are not empty
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty or contain only whitespace");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty or contain only whitespace");
        }

        User currentUser = userService.getCurrentUserFromAuthentication(authentication);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        
        // Verify user is post author
        if (!post.getAuthor().getUsername().equals(currentUser.getUsername())) {
            throw new UnauthorizedActionException(
                    "User " + currentUser.getUsername() + " is not authorized to update post " + postId);
        }

        post.setTitle(title);
        post.setContent(content);

        // Handle media files if provided
        if (mediaFiles != null && mediaFiles.length > 0) {
            int fileCount = mediaFiles.length;
            if (fileCount > MAX_MEDIA_FILES) {
                throw new IllegalArgumentException("Cannot upload more than " + MAX_MEDIA_FILES + " media files.");
            }

            // Delete old media files
            List<PostMedia> oldMedia = post.getMediaFoLES();
            for (PostMedia media : oldMedia) {
                fileStorageService.deleteFile(media.getMediaUrl());
            }
            postMediaRepository.deleteAll(oldMedia);
            post.getMediaFoLES().clear();

            // Save new media files
            int order = 0;
            for (MultipartFile mediaFile : mediaFiles) {
                PostMedia postMedia = processAndSaveMedia(mediaFile, post, order);
                if (postMedia != null) {
                    order++;
                }
            }
        }

        Post savedPost = postRepository.save(post);
        return postMapper.toDto(savedPost, currentUser);
    }

    // Delete a post (only by author)
    @Transactional
    public String deletePost(Long id, Authentication authentication) {
        validatePostAccess(id, authentication);
        String username = authentication.getName();
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        // Verify user is post author
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new UnauthorizedActionException(
                    String.format("User '%s' is not authorized to delete post %d", username, id));
        }
        
        // Delete post and associated media
        postRepository.delete(post);
        return "Post " + id + " has been deleted successfully!";
    }

    // Process and save media file to storage
    private PostMedia processAndSaveMedia(MultipartFile mediaFile, Post post, int order) {
        String contentType = mediaFile.getContentType();
        PostMedia.MediaType mediaType = null;

        // Determine media type from MIME type
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                mediaType = PostMedia.MediaType.IMAGE;
            } else if (contentType.startsWith("video/")) {
                mediaType = PostMedia.MediaType.VIDEO;
            }
        }
        
        if (mediaType != null) {
            // Store file and create PostMedia entity
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

    // Get all hidden posts for admin review
    @Transactional(readOnly = true)
    public Page<PostResponse> getHiddenPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> hiddenPosts = postRepository.findAllHiddenPosts(pageable);
        return hiddenPosts.map(post -> postMapper.toDto(post, null));
    }

    // Validate user access to post (check if hidden and user is admin)
    @Transactional(readOnly = true)
    public void validatePostAccess(Long postId, Authentication authentication) {
        User currentUser = userService.getCurrentUserFromAuthentication(authentication);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        // Block non-admin access to hidden posts
        if (post.isHidden()) {
            boolean isAdmin = currentUser != null &&
                    currentUser.getRoles().contains(Role.ADMIN);
            if (!isAdmin) {
                throw new ResourceNotFoundException("Post", "id", postId);
            }
        }
    }

}