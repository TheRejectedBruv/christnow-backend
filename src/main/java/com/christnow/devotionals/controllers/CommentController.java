package com.christnow.devotionals.controllers;

import com.christnow.devotionals.models.Comment;
import com.christnow.devotionals.models.Devotional;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.payload.CommentRequest;
import com.christnow.devotionals.repositories.DevotionalRepository;
import com.christnow.devotionals.repositories.UserRepository;
import com.christnow.devotionals.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private DevotionalRepository devotionalRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ Get comments for a devotional
    @GetMapping("/{devotionalId}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long devotionalId) {
        Devotional devotional = devotionalRepository.findById(devotionalId)
                .orElseThrow(() -> new RuntimeException("Devotional not found"));
        List<Comment> comments = commentService.getCommentsForDevotional(devotional);
        return ResponseEntity.ok(comments);
    }

    // ✅ Post a comment to a devotional
    @PostMapping("/{devotionalId}")
    public ResponseEntity<String> addComment(@PathVariable Long devotionalId,
                                              @RequestBody CommentRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Devotional devotional = devotionalRepository.findById(devotionalId)
                .orElseThrow(() -> new RuntimeException("Devotional not found"));

        commentService.addComment(user, devotional, request.getContent());

        return ResponseEntity.ok("Comment added.");
    }
}
