package com.zerooneblog.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zerooneblog.api.domain.Post;
import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.persistence.PostRepository;
import com.zerooneblog.api.infrastructure.persistence.UserRepository;
import com.zerooneblog.api.interfaces.dto.PostResponse;
import com.zerooneblog.api.interfaces.dto.UserProfileDto;
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
    public UserProfileDto getUserProfile(Long userId, Authentication authentication) {
        User user = findById(userId);
        User currentUser = getCurrentUserFromAuthentication(authentication);

        boolean isSubscribed = this.isUserSubscribedToProfile(currentUser, user);

        List<Post> posts = postRepository.findByAuthorIdAndHidden(userId, false);

        List<PostResponse> postDto = posts.stream()
                .map(post -> postMapper.toDto(post, currentUser))
                .collect(Collectors.toList());

        long followerCount = userRepository.countFollowers(userId);
        long followingCount = userRepository.countFollowing(userId);

        return new UserProfileDto(
                user.getId(),
                user.getName(),
                user.getUsername(),
                postDto,
                followerCount,
                followingCount,
                isSubscribed,
                user.isEnabled(), 
                user.getReportedCount(),
                user.getReportingCount());
    }

    @Transactional
    public String followUser(Long userId, Authentication authentication) {
        User currentUser = getCurrentUserFromAuthentication(authentication);
        User userToFollow = findById(userId);

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
    public String unfollowUser(Long userId, Authentication authentication) {
        User currentUser = getCurrentUserFromAuthentication(authentication);
        if (currentUser == null) {
            throw new UnauthorizedActionException("You must be logged in to follow a user.");
        }

        User userToUnfollow = findById(userId);

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
        return viewer.getFollowing().contains(profileUser);
    }

    public User getCurrentUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
}
