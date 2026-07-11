package com.christnow.devotionals.services;

import com.christnow.devotionals.models.Lesson;
import com.christnow.devotionals.models.LessonReflection;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.repositories.LessonReflectionRepository;
import com.christnow.devotionals.repositories.LessonRepository;
import com.christnow.devotionals.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LessonReflectionRepository lessonReflectionRepository;

    // In-memory storage for completion until persisted in the database
    private final Map<String, Set<Long>> userCompletedLessons = new HashMap<>();

    public List<Lesson> getLessonsByCourse(Long courseId) {
        return lessonRepository.findByCourseId(courseId);
    }

    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
    }

    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }

    public void markLessonComplete(Long lessonId, String username) {
        Lesson lesson = getLessonById(lessonId);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userCompletedLessons
                .computeIfAbsent(username, k -> new HashSet<>())
                .add(lessonId);
    }

    public void saveReflection(Long lessonId, String email, String reflectionText) {
        Lesson lesson = getLessonById(lessonId);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LessonReflection reflection = lessonReflectionRepository
                .findByUserIdAndLessonId(user.getId(), lessonId)
                .orElseGet(() -> {
                    LessonReflection created = new LessonReflection();
                    created.setUser(user);
                    created.setLesson(lesson);
                    return created;
                });

        reflection.setReflectionText(reflectionText != null ? reflectionText : "");
        lessonReflectionRepository.save(reflection);
    }

    // Optional: helpers for frontend to get data
    public boolean isLessonCompleted(String username, Long lessonId) {
        return userCompletedLessons.getOrDefault(username, Collections.emptySet())
                .contains(lessonId);
    }

    public String getReflection(String email, Long lessonId) {
        return userRepository.findByEmailIgnoreCase(email)
                .flatMap(user -> lessonReflectionRepository.findByUserIdAndLessonId(user.getId(), lessonId))
                .map(LessonReflection::getReflectionText)
                .orElse("");
    }
}
