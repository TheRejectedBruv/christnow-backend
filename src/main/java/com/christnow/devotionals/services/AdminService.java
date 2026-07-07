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

        String normalized = normalizeEmail(email);
        if (normalized == null) {
            return false;
        }

        for (String entry : admins.split(",")) {
            String configured = normalizeEmail(entry);
            if (configured != null && configured.equals(normalized)) {
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

    public String adminHintFor(String email) {
        if (!isAdminConfigured()) {
            return "Backend cannot see CHRISTNOW_ADMIN_EMAIL. Set it on the christnow-backend Heroku app, then restart dynos.";
        }
        if (isAdmin(email)) {
            return "OK";
        }
        String normalized = normalizeEmail(email);
        return "CHRISTNOW_ADMIN_EMAIL is set on Heroku but does not match signed-in email "
                + (normalized == null ? "(unknown)" : normalized)
                + ". Use that exact email in the config var.";
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

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        String value = email.trim();
        if (value.length() >= 2) {
            if ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1).trim();
            }
        }

        value = value.toLowerCase();
        return value.isBlank() ? null : value;
    }
}
