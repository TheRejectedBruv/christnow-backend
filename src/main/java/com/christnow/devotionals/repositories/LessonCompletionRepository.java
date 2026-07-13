package com.christnow.devotionals.repositories;

import com.christnow.devotionals.models.LessonCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, Long> {
    Optional<LessonCompletion> findByUserIdAndLessonId(Long userId, Long lessonId);

    List<LessonCompletion> findByUserIdAndLesson_CourseId(Long userId, Long courseId);
}
