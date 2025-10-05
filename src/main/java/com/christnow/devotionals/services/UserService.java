package com.christnow.devotionals.services;

import com.christnow.devotionals.models.User;
import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.repositories.CourseRepository;
import com.christnow.devotionals.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getFreeCourses().size() >= 3) {
            throw new RuntimeException("You can only select 3 free courses.");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        if (user.getFreeCourses().contains(course)) {
            throw new RuntimeException("Course already selected as free.");
        }
        user.getFreeCourses().add(course);
        userRepository.save(user);
    }
}
