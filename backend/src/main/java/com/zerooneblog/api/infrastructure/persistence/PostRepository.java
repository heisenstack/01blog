package com.zerooneblog.api.infrastructure.persistence;

import com.zerooneblog.api.domain.Post;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query(value = "SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.likes WHERE p.hidden = false", countQuery = "SELECT COUNT(p) FROM Post p WHERE p.hidden = false")
    Page<Post> findAllWithDetails(Pageable pageable);

    Page<Post> findAll(Pageable pageable);


    @Query(value = "SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.author u LEFT JOIN FETCH p.likes WHERE u.id IN :userIds AND p.hidden = false", countQuery = "SELECT COUNT(p) FROM Post p WHERE p.author.id IN :userIds AND p.hidden = false")
    Page<Post> findPostsByUserIdIn(@Param("userIds") List<Long> userIds, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.author.id = :userId AND p.hidden = false")
    Page<Post> findByUserId(@Param("userId") Long userId, Pageable pageable);

    long countByHidden(boolean hidden);

    @Query(value = "SELECT p FROM Post p LEFT JOIN FETCH p.author WHERE p.hidden = true", countQuery = "SELECT COUNT(p) FROM Post p WHERE p.hidden = true")
    Page<Post> findAllHiddenPosts(Pageable pageable);
}
