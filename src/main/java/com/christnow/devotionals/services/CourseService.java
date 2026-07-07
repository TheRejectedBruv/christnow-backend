package com.christnow.devotionals.services;

import com.christnow.devotionals.dto.CourseImportDto;
import com.christnow.devotionals.dto.ImportResultDto;
import com.christnow.devotionals.dto.LessonImportDto;
import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.models.Lesson;
import com.christnow.devotionals.exception.ResourceNotFoundException;

import com.christnow.devotionals.repositories.CourseRepository;
import com.christnow.devotionals.repositories.LessonRepository;
import com.christnow.devotionals.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    @Autowired
    public CourseService(
            CourseRepository courseRepository,
            LessonRepository lessonRepository,
            UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    public Course updateCourse(Long id, Course updatedCourse) {
        return courseRepository.findById(id).map(course -> {
            course.setTitle(updatedCourse.getTitle());
            course.setDescription(updatedCourse.getDescription());
            course.setPrice(updatedCourse.getPrice());
            course.setDifficulty(updatedCourse.getDifficulty());
            course.setFree(updatedCourse.isFree());
            course.setImageUrl(updatedCourse.getImageUrl());
            return courseRepository.save(course);
        }).orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    @Transactional
    public boolean deleteCourse(Long id) {
        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isEmpty()) {
            return false;
        }

        Course course = courseOpt.get();

        userRepository.findAll().forEach(user -> {
            boolean changed = false;
            if (user.getFreeCourses() != null && user.getFreeCourses().removeIf(c -> c.getId().equals(id))) {
                changed = true;
            }
            if (user.getOwnedCourses() != null && user.getOwnedCourses().removeIf(c -> c.getId().equals(id))) {
                changed = true;
            }
            if (changed) {
                userRepository.save(user);
            }
        });

        lessonRepository.findByCourseId(id).forEach(lessonRepository::delete);
        courseRepository.delete(course);
        return true;
    }

    public boolean existsByTitle(String title) {
        return courseRepository.existsByTitle(title);
    }

    @Transactional
    public ImportResultDto importCourses(List<CourseImportDto> imports, boolean skipDuplicates) {
        ImportResultDto result = new ImportResultDto();

        if (imports == null || imports.isEmpty()) {
            result.addMessage("No courses provided.");
            return result;
        }

        for (CourseImportDto dto : imports) {
            if (dto.getTitle() == null || dto.getTitle().isBlank()) {
                result.setSkipped(result.getSkipped() + 1);
                result.addMessage("Skipped row with missing course title.");
                continue;
            }

            Optional<Course> existing = courseRepository.findByTitle(dto.getTitle().trim());

            if (existing.isPresent()) {
                if (skipDuplicates) {
                    result.setSkipped(result.getSkipped() + 1);
                    result.addMessage("Skipped existing course: \"" + dto.getTitle() + "\"");
                    continue;
                }

                Course course = existing.get();
                applyCourseFields(course, dto);
                replaceLessons(course, dto.getLessons());
                courseRepository.save(course);
                result.setUpdated(result.getUpdated() + 1);
                result.addMessage("Updated course: \"" + course.getTitle() + "\"");
                continue;
            }

            Course course = new Course();
            applyCourseFields(course, dto);
            course.setLessons(buildLessons(course, dto.getLessons()));
            courseRepository.save(course);
            result.setCreated(result.getCreated() + 1);
            result.addMessage("Created course: \"" + course.getTitle() + "\" with "
                    + (course.getLessons() != null ? course.getLessons().size() : 0) + " lesson(s)");
        }

        return result;
    }

    public List<CourseImportDto> exportAllCourses() {
        List<Course> courses = courseRepository.findAll();
        List<CourseImportDto> exports = new ArrayList<>();

        for (Course course : courses) {
            CourseImportDto dto = new CourseImportDto();
            dto.setTitle(course.getTitle());
            dto.setDescription(course.getDescription());
            dto.setPrice(course.getPrice());
            dto.setFree(course.isFree());
            dto.setDifficulty(course.getDifficulty());
            dto.setImageUrl(course.getImageUrl());

            List<LessonImportDto> lessonDtos = new ArrayList<>();
            List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
            lessons.sort(Comparator.comparingInt(Lesson::getLessonOrder));

            for (Lesson lesson : lessons) {
                LessonImportDto lessonDto = new LessonImportDto();
                lessonDto.setTitle(lesson.getTitle());
                lessonDto.setVideoUrl(lesson.getVideoUrl());
                lessonDto.setLessonOrder(lesson.getLessonOrder());
                lessonDto.setContent(lesson.getContent());
                lessonDtos.add(lessonDto);
            }

            dto.setLessons(lessonDtos);
            exports.add(dto);
        }

        return exports;
    }

    private void applyCourseFields(Course course, CourseImportDto dto) {
        course.setTitle(dto.getTitle().trim());
        course.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        course.setPrice(dto.getPrice());
        course.setFree(dto.isFree());
        course.setDifficulty(dto.getDifficulty());
        course.setImageUrl(dto.getImageUrl());
    }

    private List<Lesson> buildLessons(Course course, List<LessonImportDto> lessonDtos) {
        List<Lesson> lessons = new ArrayList<>();
        if (lessonDtos == null) {
            return lessons;
        }

        for (LessonImportDto lessonDto : lessonDtos) {
            if (lessonDto.getTitle() == null || lessonDto.getTitle().isBlank()) {
                continue;
            }
            Lesson lesson = new Lesson();
            lesson.setCourse(course);
            lesson.setTitle(lessonDto.getTitle().trim());
            lesson.setVideoUrl(lessonDto.getVideoUrl());
            lesson.setLessonOrder(lessonDto.getLessonOrder() > 0 ? lessonDto.getLessonOrder() : lessons.size() + 1);
            lesson.setContent(lessonDto.getContent());
            lessons.add(lesson);
        }

        return lessons;
    }

    private void replaceLessons(Course course, List<LessonImportDto> lessonDtos) {
        List<Lesson> existingLessons = lessonRepository.findByCourseId(course.getId());
        lessonRepository.deleteAll(existingLessons);
        course.setLessons(buildLessons(course, lessonDtos));
    }
}
