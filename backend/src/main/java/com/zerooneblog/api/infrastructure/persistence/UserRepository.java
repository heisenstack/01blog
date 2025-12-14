package com.zerooneblog.api.infrastructure.persistence;

import com.zerooneblog.api.domain.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(f) FROM User u JOIN u.followers f WHERE u.id = :userId")
    long countFollowers(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM User u JOIN u.following f WHERE u.id = :userId")
    long countFollowing(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) FROM user_followers WHERE follower_id = :followerId AND following_id = :followingId", nativeQuery = true)
    int countByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    @Query("SELECT f.id FROM User u JOIN u.following f WHERE u.id = :userId")
    List<Long> findFollowingIds(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM user_followers WHERE follower_id = :followerId AND following_id = :followingId", nativeQuery = true)
    int deleteFollowRelationship(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    @Modifying
    @Query(value = "INSERT INTO user_followers (follower_id, following_id) VALUES (:followerId, :followingId)", nativeQuery = true)
    void insertFollowRelationship(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    long countByCreatedAtAfter(Instant date);

    long countByEnabled(boolean enabled);

}
