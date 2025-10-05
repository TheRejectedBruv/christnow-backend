package com.christnow.devotionals.services;
import com.christnow.devotionals.models.Devotional;
import com.christnow.devotionals.models.Favorite;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.repositories.DevotionalRepository;
import com.christnow.devotionals.repositories.FavoriteRepository;
import com.christnow.devotionals.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DevotionalRepository devotionalRepository;

    public List<Favorite> getFavorites(User user) {
        return favoriteRepository.findByUser(user);
    }

    public boolean addFavorite(User user, Devotional devotional) {
        if (favoriteRepository.existsByUserAndDevotional(user, devotional)) {
            return false;  // Already favorited
        }
        Favorite favorite = new Favorite(user, devotional);
        favoriteRepository.save(favorite);
        return true;
    }

    public boolean removeFavorite(User user, Devotional devotional) {
        if (!favoriteRepository.existsByUserAndDevotional(user, devotional)) {
            return false;  // Not in favorites
        }
        favoriteRepository.deleteByUserAndDevotional(user, devotional);
        return true;
    }
}
