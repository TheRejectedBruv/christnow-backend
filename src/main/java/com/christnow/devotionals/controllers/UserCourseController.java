package com.christnow.devotionals.controllers;
import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.models.UserCourse;
import com.christnow.devotionals.repositories.CourseRepository;
import com.christnow.devotionals.repositories.UserRepository;
import com.christnow.devotionals.services.UserCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/user-courses")
public class UserCourseController {

    @Autowired
    private UserCourseService userCourseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    // ✅ Get current user's enrolled courses
    @GetMapping
    public List<UserCourse> getMyCourses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userCourseService.getUserCourses(user);
    }

    // ✅ Enroll in a course
    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<String> enrollInCourse(@PathVariable Long courseId,
                                                  @RequestParam(required = false, defaultValue = "false") boolean isFree) {
      
        User user = getCurrentUser();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
         try {
        boolean success = userCourseService.enrollUserInCourse(user, course, isFree);
        if (success) {
            return ResponseEntity.ok("Enrolled successfully.");
        } else {
            return ResponseEntity.badRequest().body("Already enrolled in this course.");
        }
         } catch(RuntimeException e) {
        	 return ResponseEntity.badRequest().body(e.getMessage());
         }
    }

    // ✅ Unenroll from a course
    @DeleteMapping("/unenroll/{courseId}")
    public ResponseEntity<String> unenroll(@PathVariable Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        boolean success = userCourseService.removeUserFromCourse(user, course);
        if (success) {
            return ResponseEntity.ok("Unenrolled successfully.");
        } else {
            return ResponseEntity.badRequest().body("Not enrolled in this course.");
        }
    }
    
    @GetMapping("/owns/{courseId}") 
    public ResponseEntity<Boolean> checkIfOwnsCourse(@PathVariable Long courseId) {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	String username = authentication.getName();
    	User user = userRepository.findByUsername(username)
    			.orElseThrow(() -> new RuntimeException("User not found"));
    	Course course = courseRepository.findById(courseId)
    			.orElseThrow(() -> new RuntimeException("Course not found"));
    	boolean owns = userCourseService.userOwnsCourse(user, course);
    	return ResponseEntity.ok(owns);
    }
    
    private User getCurrentUser() {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	String username = authentication.getName();
    	return userRepository.findByUsername(username)
    			.orElseThrow(() -> new RuntimeException("User not found"));
    }
}
