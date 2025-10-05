package com.christnow.devotionals.controllers;

import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.models.UserCourseSelection;
import com.christnow.devotionals.repositories.CourseRepository;
import com.christnow.devotionals.repositories.UserRepository;
import com.christnow.devotionals.services.UserCourseSelectionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/courses")
public class UserCourseSelectionController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserCourseSelectionService courseSelectionService;

    @PostMapping("/select/{courseId}")
    public ResponseEntity<?> selectCourse(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long courseId
    ) {
        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();

        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Course not found");
        }

        Course course = courseOpt.get();

        // Check if already selected
        if (courseSelectionService.hasUserPickedCourse(user, course)) {
            return ResponseEntity.badRequest().body("Course already selected");
        }

        // Enforce 3 free course limit
        if (course.isFree()) {
            int currentFreeCount = courseSelectionService.getFreeCourseCount(user);
            if (currentFreeCount >= 3) {
                return ResponseEntity.badRequest().body("You have already selected 3 free courses");
            }
        }

        // Save the selection
        UserCourseSelection selection = new UserCourseSelection();
        selection.setUser(user);
        selection.setCourse(course);
        selection.setFreeSelection(course.isFree());

        courseSelectionService.saveCourseSelection(selection);

        return ResponseEntity.ok("Course selected successfully");
    }
}
