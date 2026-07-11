package com.christnow.devotionals.repositories;

import com.christnow.devotionals.models.LessonReflection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LessonReflectionRepository extends JpaRepository<LessonReflection, Long> {
    Optional<LessonReflection> findByUserIdAndLessonId(Long userId, Long lessonId);
}
