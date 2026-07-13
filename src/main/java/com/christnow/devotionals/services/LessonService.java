package com.christnow.devotionals.services;

import com.christnow.devotionals.models.Lesson;
import com.christnow.devotionals.models.LessonCompletion;
import com.christnow.devotionals.models.LessonReflection;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.repositories.LessonCompletionRepository;
import com.christnow.devotionals.repositories.LessonReflectionRepository;
import com.christnow.devotionals.repositories.LessonRepository;
import com.christnow.devotionals.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LessonReflectionRepository lessonReflectionRepository;

    @Autowired
    private LessonCompletionRepository lessonCompletionRepository;

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

    public void markLessonComplete(Long lessonId, String email) {
        Lesson lesson = getLessonById(lessonId);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        lessonCompletionRepository
                .findByUserIdAndLessonId(user.getId(), lessonId)
                .orElseGet(() -> {
                    LessonCompletion completion = new LessonCompletion();
                    completion.setUser(user);
                    completion.setLesson(lesson);
                    completion.setCompletedAt(Instant.now());
                    return lessonCompletionRepository.save(completion);
                });
    }

    public List<Long> getCompletedLessonIdsForCourse(String email, Long courseId) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return lessonCompletionRepository
                .findByUserIdAndLesson_CourseId(user.getId(), courseId)
                .stream()
                .map(completion -> completion.getLesson().getId())
                .toList();
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

    public boolean isLessonCompleted(String email, Long lessonId) {
        return userRepository.findByEmailIgnoreCase(email)
                .flatMap(user -> lessonCompletionRepository.findByUserIdAndLessonId(user.getId(), lessonId))
                .isPresent();
    }

    public String getReflection(String email, Long lessonId) {
        return userRepository.findByEmailIgnoreCase(email)
                .flatMap(user -> lessonReflectionRepository.findByUserIdAndLessonId(user.getId(), lessonId))
                .map(LessonReflection::getReflectionText)
                .orElse("");
    }
}
