package com.christnow.devotionals.controllers;

import com.christnow.devotionals.dto.CourseImportDto;
import com.christnow.devotionals.dto.ImportResultDto;
import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.security.AuthUtils;
import com.christnow.devotionals.services.AdminService;
import com.christnow.devotionals.services.CourseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private AdminService adminService;

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportCourses(Authentication authentication) {
        if (!canManage(authentication)) {
            return forbidden();
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
            @RequestBody List<CourseImportDto> courses,
            @RequestParam(defaultValue = "true") boolean skipDuplicates) {
        if (!canManage(authentication)) {
            return forbidden();
        }
        ImportResultDto result = courseService.importCourses(courses, skipDuplicates);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createCourse(Authentication authentication, @RequestBody Course course) {
        if (!canManage(authentication)) {
            return forbidden();
        }
        Course savedCourse = courseService.createCourse(course);
        return ResponseEntity.status(201).body(savedCourse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Course updatedCourse) {
        if (!canManage(authentication)) {
            return forbidden();
        }
        Course updated = courseService.updateCourse(id, updatedCourse);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(Authentication authentication, @PathVariable Long id) {
        if (!canManage(authentication)) {
            return forbidden();
        }
        if (courseService.deleteCourse(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean canManage(Authentication authentication) {
        return adminService.canManageCourses(AuthUtils.resolveEmail(authentication));
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Admin access required. Set CHRISTNOW_ADMIN_EMAIL on Heroku to your sign-in email.");
    }
}
