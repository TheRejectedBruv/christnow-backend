package com.christnow.devotionals.controllers;


import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.christnow.devotionals.services.AdminService;


@RestController
public class HealthController {

    private final AdminService adminService;

    public HealthController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "OK",
                "build", "2026-07-07-v4",
                "adminConfigured", adminService.isAdminConfigured()
        );
    }
}
