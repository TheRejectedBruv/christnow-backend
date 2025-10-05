package com.christnow.devotionals.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.christnow.devotionals.models.Devotional;
import com.christnow.devotionals.models.User;


public interface DevotionalRepository extends JpaRepository<Devotional, Long> {
	
	List<Devotional> findByUser(User user);
}
