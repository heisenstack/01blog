package com.zerooneblog.api.infrastructure.persistence;

import com.zerooneblog.api.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
// import java.util.List;
// import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>{
    // Post findById(Long id);
}
