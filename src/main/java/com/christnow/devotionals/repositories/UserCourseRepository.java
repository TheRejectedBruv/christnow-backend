package com.christnow.devotionals.repositories;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.models.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long>{
    List<UserCourse> findByUser(User user);

    Optional<UserCourse> findByUserAndCourse(User user, Course course);

    boolean existsByUserAndCourse(User user, Course course);
    
    long countByUserAndIsFreeCourseTrue(User user);
}
