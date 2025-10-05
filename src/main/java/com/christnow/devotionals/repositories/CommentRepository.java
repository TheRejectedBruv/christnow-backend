package com.christnow.devotionals.repositories;
import com.christnow.devotionals.models.Comment;
import com.christnow.devotionals.models.Devotional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository  extends JpaRepository<Comment, Long>{
	 List<Comment> findByDevotional(Devotional devotional);
}
