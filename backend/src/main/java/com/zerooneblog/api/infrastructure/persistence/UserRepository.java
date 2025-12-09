package com.zerooneblog.api.infrastructure.persistence;

import com.zerooneblog.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String email);


    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
