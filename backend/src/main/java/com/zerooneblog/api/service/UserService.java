package com.zerooneblog.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zerooneblog.api.domain.Post;
import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.persistence.PostRepository;
import com.zerooneblog.api.infrastructure.persistence.UserRepository;
import com.zerooneblog.api.interfaces.dto.PostResponse;
import com.zerooneblog.api.interfaces.dto.PostsResponseDto;
import com.zerooneblog.api.interfaces.dto.UserProfileDto;
import com.zerooneblog.api.interfaces.dto.UserSuggestionDto;
import com.zerooneblog.api.interfaces.dto.UserSuggestionResponse;
import com.zerooneblog.api.interfaces.exception.DuplicateResourceException;
import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;
import com.zerooneblog.api.interfaces.exception.UnauthorizedActionException;
import com.zerooneblog.api.service.mapper.PostMapper;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public UserService(UserRepository userRepository, PostRepository postRepository, PostMapper postMapper) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postMapper = postMapper;
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