package com.christnow.devotionals.services;

import com.christnow.devotionals.models.Lesson;
import com.christnow.devotionals.models.User;
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

    // Example in-memory storage for completion & reflections
    // Replace with database tables/entities later for real persistence
    private final Map<String, Set<Long>> userCompletedLessons = new HashMap<>();
    private final Map<String, Map<Long, String>> userReflections = new HashMap<>();

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

    public void saveReflection(Long lessonId, String username, String reflectionText) {
        Lesson lesson = getLessonById(lessonId);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userReflections
                .computeIfAbsent(username, k -> new HashMap<>())
                .put(lessonId, reflectionText);
    }

    // Optional: helpers for frontend to get data
    public boolean isLessonCompleted(String username, Long lessonId) {
        return userCompletedLessons.getOrDefault(username, Collections.emptySet())
                .contains(lessonId);
    }

    public String getReflection(String username, Long lessonId) {
        return userReflections.getOrDefault(username, Collections.emptyMap())
                .getOrDefault(lessonId, "");
    }
}
