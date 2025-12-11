package com.zerooneblog.api.infrastructure.persistence;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zerooneblog.api.domain.PostMedia;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {

}
