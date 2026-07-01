package com.christnow.devotionals.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.christnow.devotionals.repositories.CourseRepository;

@Service
public class AdminService {

    private final CourseRepository courseRepository;
    private final String configuredAdminEmails;

    public AdminService(
            CourseRepository courseRepository,
            @Value("${CHRISTNOW_ADMIN_EMAIL:}") String configuredAdminEmails) {
        this.courseRepository = courseRepository;
        this.configuredAdminEmails = configuredAdminEmails;
    }

    public boolean isAdmin(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String admins = resolveAdminEmails();
        if (admins.isBlank()) {
            return false;
        }

        String normalized = email.trim().toLowerCase();
        for (String entry : admins.split(",")) {
            if (entry.trim().toLowerCase().equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    /** Admin email match, or one-time bootstrap while the course database is empty. */
    public boolean canManageCourses(String email) {
        if (isAdmin(email)) {
            return true;
        }
        return email != null && !email.isBlank() && courseRepository.count() == 0;
    }

    public boolean isCoursesEmpty() {
        return courseRepository.count() == 0;
    }

    public boolean isAdminConfigured() {
        return !resolveAdminEmails().isBlank();
    }

    private String resolveAdminEmails() {
        String fromEnv = System.getenv("CHRISTNOW_ADMIN_EMAIL");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        if (configuredAdminEmails != null && !configuredAdminEmails.isBlank()) {
            return configuredAdminEmails.trim();
        }
        return "";
    }
}
