package com.christnow.devotionals.controllers;

import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.models.Lesson;
import com.christnow.devotionals.repositories.CourseRepository;
import com.christnow.devotionals.repositories.LessonRepository;
import com.christnow.devotionals.security.AuthUtils;
import com.christnow.devotionals.security.JwtUtil;
import com.christnow.devotionals.services.AdminService;
import com.christnow.devotionals.services.CourseService;
import com.christnow.devotionals.services.LessonService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/lessons")
public class LessonController {

    @Autowired
    private LessonService lessonService;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private JwtUtil jwtUtil;

    // Get all lessons for a specific course
    @GetMapping("/by-course/{courseId}")
    public List<Lesson> getLessonsByCourse(@PathVariable Long courseId) {
        return lessonService.getLessonsByCourse(courseId);
    }

    // Create a new lesson
    @PostMapping
    public ResponseEntity<?> createLesson(
            Authentication authentication,
            HttpServletRequest request,
            @RequestBody Lesson lesson) {
        if (!canManage(authentication, request)) {
            return adminForbidden(authentication, request);
        }
        Long courseId = lesson.getCourse().getId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        lesson.setCourse(course);
        return ResponseEntity.ok(lessonRepository.save(lesson));
    }

    @PostMapping("/delete-course/{courseId}")
    public ResponseEntity<?> deleteCourseById(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable Long courseId) {
        if (!canManage(authentication, request)) {
            return adminForbidden(authentication, request);
        }
        if (courseService.deleteCourse(courseId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Get lesson by id
    @GetMapping("/{id}")
    public Lesson getLessonById(@PathVariable Long id) {
        return lessonService.getLessonById(id);
    }

    // Delete lesson by id
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLesson(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable Long id) {
        if (!canManage(authentication, request)) {
            return adminForbidden(authentication, request);
        }
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    // Mark lesson complete
    @PostMapping("/{lessonId}/complete")
    public ResponseEntity<?> markLessonComplete(@PathVariable Long lessonId, Principal principal) {
        lessonService.markLessonComplete(lessonId, principal.getName());
        return ResponseEntity.ok().body("Lesson marked as complete.");
    }

    // Save reflection
    @PostMapping("/{lessonId}/reflection")
    public ResponseEntity<?> saveReflection(@PathVariable Long lessonId,
                                            @RequestBody String reflectionText,
                                            Principal principal) {
        lessonService.saveReflection(lessonId, principal.getName(), reflectionText);
        return ResponseEntity.ok().body("Reflection saved.");
    }

    private boolean canManage(Authentication authentication, HttpServletRequest request) {
        return adminService.canManageCourses(AuthUtils.resolveEmail(authentication, request, jwtUtil));
    }

    private ResponseEntity<String> adminForbidden(Authentication authentication, HttpServletRequest request) {
        String email = AuthUtils.resolveEmail(authentication, request, jwtUtil);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Sign-in required. No valid login token was received.");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(adminService.adminHintFor(email));
    }
}
