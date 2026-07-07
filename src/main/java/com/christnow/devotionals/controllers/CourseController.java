package com.christnow.devotionals.controllers;

import com.christnow.devotionals.dto.CourseImportDto;
import com.christnow.devotionals.dto.ImportResultDto;
import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.security.AuthUtils;
import com.christnow.devotionals.security.JwtUtil;
import com.christnow.devotionals.services.AdminService;
import com.christnow.devotionals.services.CourseService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportCourses(Authentication authentication, HttpServletRequest request) {
        if (!canManage(authentication, request)) {
            return forbidden(authentication, request);
        }
        return ResponseEntity.ok(courseService.exportAllCourses());
    }

    @GetMapping("/exists")
    public boolean existsByTitle(@RequestParam String title) {
        return courseService.existsByTitle(title);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        Optional<Course> course = courseService.getCourseById(id);
        return course.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/import")
    public ResponseEntity<?> importCourses(
            Authentication authentication,
            HttpServletRequest request,
            @RequestBody List<CourseImportDto> courses,
            @RequestParam(defaultValue = "true") boolean skipDuplicates) {
        if (!canManage(authentication, request)) {
            return forbidden(authentication, request);
        }
        ImportResultDto result = courseService.importCourses(courses, skipDuplicates);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createCourse(
            Authentication authentication,
            HttpServletRequest request,
            @RequestBody Course course) {
        if (!canManage(authentication, request)) {
            return forbidden(authentication, request);
        }
        Course savedCourse = courseService.createCourse(course);
        return ResponseEntity.status(201).body(savedCourse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Course updatedCourse) {
        if (!canManage(authentication, request)) {
            return forbidden(authentication, request);
        }
        Course updated = courseService.updateCourse(id, updatedCourse);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable Long id) {
        return deleteCourseInternal(authentication, request, id);
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<?> deleteCoursePost(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable Long id) {
        return deleteCourseInternal(authentication, request, id);
    }

    private ResponseEntity<?> deleteCourseInternal(
            Authentication authentication,
            HttpServletRequest request,
            Long id) {
        if (!canManage(authentication, request)) {
            return forbidden(authentication, request);
        }
        if (courseService.deleteCourse(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean canManage(Authentication authentication, HttpServletRequest request) {
        return adminService.canManageCourses(AuthUtils.resolveEmail(authentication, request, jwtUtil));
    }

    private ResponseEntity<?> forbidden(Authentication authentication, HttpServletRequest request) {
        String email = AuthUtils.resolveEmail(authentication, request, jwtUtil);
        String message;
        if (email == null) {
            message = "Sign-in required. This request did not include a valid login token.";
        } else if (!adminService.isAdminConfigured()) {
            message = "Admin access required for " + email
                    + ". Set CHRISTNOW_ADMIN_EMAIL on Heroku to that exact email.";
        } else if (!adminService.isAdmin(email)) {
            message = "Admin access required for " + email
                    + ". CHRISTNOW_ADMIN_EMAIL on Heroku must include that exact email.";
        } else {
            message = "Admin access required.";
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
    }
}
