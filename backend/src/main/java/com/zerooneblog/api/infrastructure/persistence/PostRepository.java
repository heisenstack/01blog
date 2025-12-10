package com.zerooneblog.api.infrastructure.persistence;

import com.zerooneblog.api.domain.Post;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>{
    List<Post> findByAuthorIdAndHidden(Long userId, boolean isHidden);
}
