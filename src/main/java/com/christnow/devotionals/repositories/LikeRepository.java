package com.christnow.devotionals.repositories;
import com.christnow.devotionals.models.Like;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.models.Devotional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long>{
	   List<Like> findByDevotional(Devotional devotional);

	    Optional<Like> findByUserAndDevotional(User user, Devotional devotional);

	    boolean existsByUserAndDevotional(User user, Devotional devotional);

	    void deleteByUserAndDevotional(User user, Devotional devotional);

	    long countByDevotional(Devotional devotional);
}
