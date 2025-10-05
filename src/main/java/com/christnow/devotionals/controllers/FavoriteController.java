package com.christnow.devotionals.controllers;
import com.christnow.devotionals.models.Devotional;
import com.christnow.devotionals.models.Favorite;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.repositories.DevotionalRepository;
import com.christnow.devotionals.repositories.UserRepository;
import com.christnow.devotionals.services.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {
	@Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DevotionalRepository devotionalRepository;

    // 📜 Get all favorites for the current user
    @GetMapping
    public List<Favorite> getMyFavorites() {
        User user = getCurrentUser();
        return favoriteService.getFavorites(user);
    }

    // ⭐ Add a devotional to favorites
    @PostMapping("/add/{devotionalId}")
    public ResponseEntity<String> addFavorite(@PathVariable Long devotionalId) {
        User user = getCurrentUser();
        Devotional devotional = devotionalRepository.findById(devotionalId)
                .orElseThrow(() -> new RuntimeException("Devotional not found"));

        boolean success = favoriteService.addFavorite(user, devotional);
        if (success) {
            return ResponseEntity.ok("Added to favorites.");
        } else {
            return ResponseEntity.badRequest().body("Already in favorites.");
        }
    }

    // ❌ Remove a devotional from favorites
    @DeleteMapping("/remove/{devotionalId}")
    public ResponseEntity<String> removeFavorite(@PathVariable Long devotionalId) {
        User user = getCurrentUser();
        Devotional devotional = devotionalRepository.findById(devotionalId)
                .orElseThrow(() -> new RuntimeException("Devotional not found"));

        boolean success = favoriteService.removeFavorite(user, devotional);
        if (success) {
            return ResponseEntity.ok("Removed from favorites.");
        } else {
            return ResponseEntity.badRequest().body("Devotional not in favorites.");
        }
    }

    // 🔒 Helper method to get the logged-in user
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
