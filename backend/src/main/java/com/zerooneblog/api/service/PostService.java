package com.zerooneblog.api.service;

import com.zerooneblog.api.domain.Post;
import com.zerooneblog.api.domain.User;

import com.zerooneblog.api.infrastructure.persistence.PostRepository;
import com.zerooneblog.api.infrastructure.persistence.UserRepository;
import com.zerooneblog.api.interfaces.dto.PostDTO;
import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
// import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Post createPost(PostDTO request, String username) {
        User author = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Post newPost = new Post();
        newPost.setTitle(request.getTitle());
        newPost.setContent(request.getContent());
        newPost.setAuthor(author);

        return postRepository.save(newPost);
    }
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }
   public Post getPostById(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> 
            new ResourceNotFoundException("Post", "id", postId)
        );
    }
}
