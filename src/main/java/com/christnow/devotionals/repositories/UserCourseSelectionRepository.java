package com.christnow.devotionals.repositories;

import com.christnow.devotionals.models.User;
import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.models.UserCourseSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCourseSelectionRepository  extends JpaRepository<UserCourseSelection, Long>{


    // Get all courses a user has picked as free
    List<UserCourseSelection> findByUserAndFreeSelectionTrue(User user);

    // Check if the user has already picked a specific course
    boolean existsByUserAndCourse(User user, Course course);
}
