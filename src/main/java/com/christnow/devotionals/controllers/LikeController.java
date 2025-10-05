package com.christnow.devotionals.controllers;

import com.christnow.devotionals.models.Devotional;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.models.Like;
import com.christnow.devotionals.repositories.DevotionalRepository;
import com.christnow.devotionals.repositories.UserRepository;
import com.christnow.devotionals.services.LikeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DevotionalRepository devotionalRepository;

    // ✅ Get likes for a devotional
    @GetMapping("/{devotionalId}")
    public ResponseEntity<List<Like>> getLikes(@PathVariable Long devotionalId) {
        Devotional devotional = devotionalRepository.findById(devotionalId)
                .orElseThrow(() -> new RuntimeException("Devotional not found"));

        List<Like> likes = likeService.getLikesForDevotional(devotional);
        return ResponseEntity.ok(likes);
    }

    // ✅ Like a devotional
    @PostMapping("/{devotionalId}")
    public ResponseEntity<String> likeDevotional(@PathVariable Long devotionalId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Devotional devotional = devotionalRepository.findById(devotionalId)
                .orElseThrow(() -> new RuntimeException("Devotional not found"));

        boolean success = likeService.addLike(user, devotional);

        if (success) {
            return ResponseEntity.ok("Devotional liked.");
        } else {
            return ResponseEntity.badRequest().body("You already liked this devotional.");
        }
    }

    // ✅ Remove like from a devotional
    @DeleteMapping("/{devotionalId}")
    public ResponseEntity<String> unlikeDevotional(@PathVariable Long devotionalId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Devotional devotional = devotionalRepository.findById(devotionalId)
                .orElseThrow(() -> new RuntimeException("Devotional not found"));

        boolean success = likeService.removeLike(user, devotional);

        if (success) {
            return ResponseEntity.ok("Like removed.");
        } else {
            return ResponseEntity.badRequest().body("You have not liked this devotional.");
        }
    }

    // ✅ Count total likes on a devotional
    @GetMapping("/{devotionalId}/count")
    public ResponseEntity<Long> countLikes(@PathVariable Long devotionalId) {
        Devotional devotional = devotionalRepository.findById(devotionalId)
                .orElseThrow(() -> new RuntimeException("Devotional not found"));

        long count = likeService.countLikes(devotional);
        return ResponseEntity.ok(count);
    }
}
