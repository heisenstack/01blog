package com.zerooneblog.api.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zerooneblog.api.domain.PostMedia;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {

    @Query("SELECT pm FROM PostMedia pm WHERE pm.post.id = :postId ORDER BY pm.displayOrder ASC")
    List<PostMedia> findByPostId(@Param("postId") Long postId);

}
