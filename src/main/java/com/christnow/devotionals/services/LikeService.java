package com.christnow.devotionals.services;

import com.christnow.devotionals.models.Devotional;
import com.christnow.devotionals.models.Like;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.repositories.DevotionalRepository;
import com.christnow.devotionals.repositories.LikeRepository;
import com.christnow.devotionals.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DevotionalRepository devotionalRepository;

    // ✅ Get all likes for a devotional
    public List<Like> getLikesForDevotional(Devotional devotional) {
        return likeRepository.findByDevotional(devotional);
    }

    // ✅ Add a like
    public boolean addLike(User user, Devotional devotional) {
        if (likeRepository.existsByUserAndDevotional(user, devotional)) {
            return false;  // Already liked
        }
        Like like = new Like(user, devotional);
        likeRepository.save(like);
        return true;
    }

    // ✅ Remove a like
    public boolean removeLike(User user, Devotional devotional) {
        if (!likeRepository.existsByUserAndDevotional(user, devotional)) {
            return false;  // Not liked yet
        }
        likeRepository.deleteByUserAndDevotional(user, devotional);
        return true;
    }

    // ✅ Check if user liked this devotional
    public boolean isLikedByUser(User user, Devotional devotional) {
        return likeRepository.existsByUserAndDevotional(user, devotional);
    }

    // ✅ Count total likes for a devotional
    public long countLikes(Devotional devotional) {
        return likeRepository.countByDevotional(devotional);
    }
}
