package com.zerooneblog.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zerooneblog.api.domain.Notification;
import com.zerooneblog.api.domain.Post;
import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.persistence.PostRepository;
import com.zerooneblog.api.infrastructure.persistence.UserRepository;
import com.zerooneblog.api.interfaces.dto.*;
import com.zerooneblog.api.interfaces.exception.*;
import com.zerooneblog.api.service.mapper.PostMapper;

// Service for managing user profiles, follows, and suggestions
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository, PostRepository postRepository, PostMapper postMapper,
            NotificationService notificationService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.notificationService = notificationService;
    }

    // Find user by username
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    // Find user by ID
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    // Get user profile with paginated posts and follower information
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String username, int page, int size, Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        User user = findByUsername(username);
        User currentUser = getCurrentUserFromAuthentication(authentication);

        // Check if current user is following this user
        boolean isSubscribed = this.isUserSubscribedToProfile(currentUser, user);

        // Get paginated posts from user
        Page<Post> postsPage = postRepository.findByUserId(user.getId(), pageable);

        List<PostResponse> postDtos = postsPage.getContent().stream()
                .map(post -> postMapper.toDto(post, currentUser))
                .collect(Collectors.toList());

        PostsResponseDto postsResponseDto = new PostsResponseDto(
                postDtos,
                postsPage.getNumber(),
                postsPage.getSize(),
                postsPage.getTotalElements(),
                postsPage.getTotalPages(),
                postsPage.isLast());

        // Get follower and following counts
        long followerCount = userRepository.countFollowers(user.getId());
        long followingCount = userRepository.countFollowing(user.getId());

        return new UserProfileDto(
                user.getId(),
                user.getName(),
                user.getUsername(),
                postsResponseDto,
                followerCount,
                followingCount,
                isSubscribed,
                user.isEnabled(),
                user.getReportedCount(),
                user.getReportingCount());
    }

    // Follow a user (create follow relationship)
    @Transactional
    public String followUser(String username, Authentication authentication) {
        User currentUser = getCurrentUserFromAuthentication(authentication);
        User userToFollow = findByUsername(username);

        // Prevent self-follow
        if (currentUser.getId().equals(userToFollow.getId())) {
            throw new UnauthorizedActionException("You cannot follow yourself.");
        }

        // Check if already following
        boolean alreadyFollowing = userRepository.countByFollowerIdAndFollowingId(currentUser.getId(),
                userToFollow.getId()) > 0;

        if (alreadyFollowing) {
            throw new DuplicateResourceException("You are already following " + userToFollow.getUsername() + ".");
        }

        // Create follow relationship
        userRepository.insertFollowRelationship(currentUser.getId(), userToFollow.getId());
        
        // Notify followed user
        String message = currentUser.getUsername() + " started following you.";
        notificationService.createNotification(userToFollow, currentUser, Notification.NotificationType.NEW_FOLLOWER,
                message, null);

        return "You've followed " + userToFollow.getUsername() + " successfully!";
    }

    // Unfollow a user (delete follow relationship)
    @Transactional
    public String unfollowUser(String username, Authentication authentication) {
        User currentUser = getCurrentUserFromAuthentication(authentication);
        if (currentUser == null) {
            throw new UnauthorizedActionException("You must be logged in to unfollow a user.");
        }

        User userToUnfollow = findByUsername(username);

        // Prevent self-unfollow
        if (currentUser.getId().equals(userToUnfollow.getId())) {
            throw new UnauthorizedActionException("You cannot unfollow yourself.");
        }

        // Check if currently following
        boolean isCurrentlyFollowing = userRepository.countByFollowerIdAndFollowingId(
                currentUser.getId(),
                userToUnfollow.getId()) > 0;

        if (!isCurrentlyFollowing) {
            throw new IllegalStateException("You are not following " + userToUnfollow.getUsername() + ".");
        }

        // Delete follow relationship
        userRepository.deleteFollowRelationship(currentUser.getId(), userToUnfollow.getId());

        return "You've unfollowed " + userToUnfollow.getUsername() + " successfully!";
    }

    // Check if viewer is following profile user
    @Transactional(readOnly = true)
    private boolean isUserSubscribedToProfile(User viewer, User profileUser) {
        if (viewer == null || viewer.getId().equals(profileUser.getId())) {
            return false;
        }
        return userRepository.isFollowing(viewer.getId(), profileUser.getId());
    }

    // Extract authenticated user from Authentication object
    public User getCurrentUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    // Get suggested users to follow (excluding already followed users)
    @Transactional(readOnly = true)
    public UserSuggestionResponse getSuggestedUsers(int page, int size) {
        User currentUser = getCurrentUser();

        // Get list of users current user already follows
        List<Long> followingIds = userRepository.findFollowingIds(currentUser.getId());

        if (followingIds.isEmpty()) {
            followingIds.add(-1L);
        }

        Pageable pageable = PageRequest.of(page, size);
        // Get suggested users excluding self and already followed users
        Page<User> suggestedUsersPage = userRepository.findSuggestedUsers(
                currentUser.getId(),
                followingIds,
                pageable);

        List<UserSuggestionDto> suggestions = suggestedUsersPage.getContent().stream()
                .map(user -> toUserSuggestionDto(user, false))
                .collect(Collectors.toList());

        return new UserSuggestionResponse(
                suggestions,
                suggestedUsersPage.getNumber(),
                suggestedUsersPage.getSize(),
                suggestedUsersPage.getTotalElements(),
                suggestedUsersPage.getTotalPages(),
                suggestedUsersPage.isLast());
    }

    // Get list of users current user is following
    @Transactional(readOnly = true)
    public UserSuggestionResponse getFollowingUsers(int page, int size) {
        User currentUser = getCurrentUser();

        Pageable pageable = PageRequest.of(page, size);
        Page<User> followingPage = userRepository.findFollowingByUserId(currentUser.getId(), pageable);

        List<UserSuggestionDto> following = followingPage.getContent().stream()
                .map(user -> toUserSuggestionDto(user, true))
                .collect(Collectors.toList());

        return new UserSuggestionResponse(
                following,
                followingPage.getNumber(),
                followingPage.getSize(),
                followingPage.getTotalElements(),
                followingPage.getTotalPages(),
                followingPage.isLast());
    }

    // Convert User entity to UserSuggestionDto
    private UserSuggestionDto toUserSuggestionDto(User user, boolean subscribed) {
        long followerCount = userRepository.countFollowers(user.getId());
        return new UserSuggestionDto(
                user.getId(),
                user.getUsername(),
                user.getName(),
                followerCount,
                subscribed);
    }

    // Get current authenticated user from security context
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(username);
    }
}