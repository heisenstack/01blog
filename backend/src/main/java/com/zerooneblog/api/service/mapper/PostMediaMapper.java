package com.zerooneblog.api.service.mapper;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.zerooneblog.api.domain.PostMedia;
import com.zerooneblog.api.interfaces.dto.PostMediaDto;


@Component
public class PostMediaMapper {
        public PostMediaDto toDto(PostMedia media) {
        PostMediaDto dto = new PostMediaDto();
        dto.setId(media.getId());
        dto.setDisplayOrder(media.getDisplayOrder());
        dto.setMediaType(media.getMediaType().name());
        dto.setCreatedAt(media.getCreatedAt());

        String mediaUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(media.getMediaUrl())
                .toUriString();
        dto.setMediaUrl(mediaUrl);

        return dto;
    }
}
