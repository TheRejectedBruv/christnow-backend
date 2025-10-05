package com.christnow.devotionals.repositories;

import com.christnow.devotionals.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // 🔍 Find a course by its title
    Optional<Course> findByTitle(String title);
    boolean existsByTitle(String title);
}
