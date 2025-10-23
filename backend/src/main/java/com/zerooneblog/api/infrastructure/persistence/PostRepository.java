package com.zerooneblog.api.infrastructure.persistence;

import com.zerooneblog.api.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>{
    
}
