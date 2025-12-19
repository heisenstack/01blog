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
            for (MultipartFile mediaFile : request.getMediaFiles()) {
                PostMedia postMedia = processAndSaveMedia(mediaFile, savedPost, order);
                if (postMedia != null) {
                    order++;
                }
            }
        }

        savedPost.getMediaFoLES().size();
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

    @Transactional(readOnly = true)
    public PostsResponseDto getFeedForCurrentUser(int page, int size, Authentication authentication) {
        User currentUser = userService.getCurrentUserFromAuthentication(authentication);

        List<Long> followedUserIds = userRepository.findFollowingIds(currentUser.getId());
        followedUserIds.add(currentUser.getId());

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

    public PostResponse getPostById(Long postId, Authentication authentication) {
        User currentUser = userService.getCurrentUserFromAuthentication(authentication);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        // System.out.println(post.getReportedCount());
        return postMapper.toDto(post, currentUser);
    }

    @Transactional
    public PostResponse updatePost(Long postId, String title, String content, Authentication authentication) {

        User currentUser = userService.getCurrentUserFromAuthentication(authentication);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        if (!post.getAuthor().getUsername().equals(currentUser.getUsername())) {
            throw new UnauthorizedActionException(
                    "User " + currentUser.getUsername() + " is not authorized to update post " + postId);
        }
        post.setTitle(title);
        post.setContent(content);
        Post savedPost = postRepository.save(post);
        return postMapper.toDto(savedPost, currentUser);
    }

    public String deletePost(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        if (!post.getAuthor().getUsername().equals(username)) {
            throw new UnauthorizedActionException(
                    String.format("User '%s' is not authorized to delete post %d", username, id));
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
            } else if (contentType.startsWith("video/")) {
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

    @Transactional(readOnly = true)
    public Page<PostResponse> getHiddenPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> hiddenPosts = postRepository.findAllHiddenPosts(pageable);
        return hiddenPosts.map(post -> postMapper.toDto(post, null));
    }

}
