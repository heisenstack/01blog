package com.zerooneblog.api.infrastructure.persistence;

import com.zerooneblog.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
