package com.christnow.devotionals.services;

import com.christnow.devotionals.models.User;
import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.repositories.CourseRepository;
import com.christnow.devotionals.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private CourseRepository courseRepository;
    
    

    public User registerUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already taken");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // THIS IS THE METHOD YOU NEED
    public void addFreeCourseToUser(String email, Long courseId) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        if (ownsCourse(user, courseId)) {
            throw new RuntimeException("You already own this course.");
        }
        if (user.getFreeCourses().size() >= 3) {
            throw new RuntimeException("You can only select 3 free courses.");
        }
        if (user.getFreeCourses().stream().anyMatch(c -> courseId.equals(c.getId()))) {
            throw new RuntimeException("Course already selected as free.");
        }
        user.getFreeCourses().add(course);
        userRepository.save(user);
    }

    @Transactional
    public void addOwnedCourseByEmail(String email, Long courseId) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (ownsCourse(user, courseId)) {
            return;
        }

        user.getOwnedCourses().add(course);
        userRepository.saveAndFlush(user);
    }

    private boolean ownsCourse(User user, Long courseId) {
        return user.getOwnedCourses().stream().anyMatch(c -> courseId.equals(c.getId()))
                || user.getFreeCourses().stream().anyMatch(c -> courseId.equals(c.getId()));
    }
    public UserService(PasswordEncoder passwordEncoder /*, other deps */) {
        this.passwordEncoder = passwordEncoder;
    }

}
