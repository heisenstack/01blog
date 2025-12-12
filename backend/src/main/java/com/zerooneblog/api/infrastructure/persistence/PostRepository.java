package com.zerooneblog.api.infrastructure.persistence;

import com.zerooneblog.api.domain.Post;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>{
    @Query(value = "SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.likes WHERE p.hidden = false",
    countQuery = "SELECT COUNT(p) FROM Post p WHERE p.hidden = false")
    Page<Post> findAllWithDetails(Pageable pageable);
    
    
    List<Post> findByAuthorIdAndHidden(Long userId, boolean isHidden);
}
