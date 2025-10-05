package com.christnow.devotionals.repositories;

import com.christnow.devotionals.models.Favorite;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.models.Devotional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long>{
    // Find all favorites for a user
    List<Favorite> findByUser(User user);

    // Check if a user has already favorited a devotional
    Optional<Favorite> findByUserAndDevotional(User user, Devotional devotional);

    // Check if exists
    boolean existsByUserAndDevotional(User user, Devotional devotional);

    // Delete a favorite by user and devotional
    void deleteByUserAndDevotional(User user, Devotional devotional);
}
