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

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository, PostRepository postRepository, PostMapper postMapper, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.notificationService = notificationService;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String username, int page, int size, Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        User user = findByUsername(username);
        User currentUser = getCurrentUserFromAuthentication(authentication);

        boolean isSubscribed = this.isUserSubscribedToProfile(currentUser, user);

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

    @Transactional
    public String followUser(String username, Authentication authentication) {
        User currentUser = getCurrentUserFromAuthentication(authentication);
        User userToFollow = findByUsername(username);

        if (currentUser.getId().equals(userToFollow.getId())) {
            throw new UnauthorizedActionException("You cannot follow yourself.");
        }

        boolean alreadyFollowing = userRepository.countByFollowerIdAndFollowingId(currentUser.getId(),
                userToFollow.getId()) > 0;

        if (alreadyFollowing) {
            throw new DuplicateResourceException("You are already following " + userToFollow.getUsername() + ".");
        }

        userRepository.insertFollowRelationship(currentUser.getId(), userToFollow.getId());
        String message = currentUser.getUsername() + " started following you.";

        notificationService.createNotification(userToFollow, currentUser, Notification.NotificationType.NEW_FOLLOWER, message, null);

        return "You've followed " + userToFollow.getUsername() + " successfully!";
    }

    @Transactional
    public String unfollowUser(String username, Authentication authentication) {
        User currentUser = getCurrentUserFromAuthentication(authentication);
        if (currentUser == null) {
            throw new UnauthorizedActionException("You must be logged in to follow a user.");
        }

        User userToUnfollow = findByUsername(username);

        if (currentUser.getId().equals(userToUnfollow.getId())) {
            throw new UnauthorizedActionException("You cannot unfollow yourself.");
        }

        boolean isCurrentlyFollowing = userRepository.countByFollowerIdAndFollowingId(currentUser.getId(),
                userToUnfollow.getId()) > 0;
        if (!isCurrentlyFollowing) {
            throw new ResourceNotFoundException(
                    "Follow Relationship",
                    "follower/following",
                    currentUser.getUsername() + "/" + userToUnfollow.getUsername());
        }

        userRepository.deleteFollowRelationship(currentUser.getId(), userToUnfollow.getId());

        return "You've unfollowed " + userToUnfollow.getUsername() + " successfully!";
    }

    @Transactional(readOnly = true)
    private boolean isUserSubscribedToProfile(User viewer, User profileUser) {
        if (viewer == null || viewer.getId().equals(profileUser.getId())) {
            return false;
        }
        return userRepository.isFollowing(viewer.getId(), profileUser.getId());
    }

    public User getCurrentUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @Transactional(readOnly = true)
    public UserSuggestionResponse getSuggestedUsers(int page, int size) {
        User currentUser = getCurrentUser();

        List<Long> followingIds = userRepository.findFollowingIds(currentUser.getId());

        if (followingIds.isEmpty()) {
            followingIds.add(-1L);
        }

        Pageable pageable = PageRequest.of(page, size);
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
        followingPage.isLast()
    );
}

    private UserSuggestionDto toUserSuggestionDto(User user, boolean subscribed) {
        long followerCount = userRepository.countFollowers(user.getId());
        return new UserSuggestionDto(
            user.getId(),
            user.getUsername(),
            user.getName(),
            followerCount,
            subscribed
        );
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(username);
    }
}