package com.christnow.devotionals.config;

import com.christnow.devotionals.dto.CourseImportDto;
import com.christnow.devotionals.dto.ImportResultDto;
import com.christnow.devotionals.repositories.CourseRepository;
import com.christnow.devotionals.services.CourseService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class CourseSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CourseSeedRunner.class);
    private static final String SEED_FILE = "christnow-courses-import.json";

    private final CourseRepository courseRepository;
    private final CourseService courseService;
    private final ObjectMapper objectMapper;

    public CourseSeedRunner(
            CourseRepository courseRepository,
            CourseService courseService,
            ObjectMapper objectMapper) {
        this.courseRepository = courseRepository;
        this.courseService = courseService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (courseRepository.count() > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource(SEED_FILE);
        if (!resource.exists()) {
            log.info("Course database empty but {} not found on classpath; skipping auto-seed.", SEED_FILE);
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            List<CourseImportDto> courses = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<CourseImportDto>>() {});

            ImportResultDto result = courseService.importCourses(courses, false);
            log.info(
                    "Auto-seeded {} course(s): {} created, {} updated, {} skipped.",
                    courses.size(),
                    result.getCreated(),
                    result.getUpdated(),
                    result.getSkipped());
        }
    }
}
