package com.zerooneblog.api.infrastructure.persistence;

import com.zerooneblog.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
// import java.util.List;


public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String email);


    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
