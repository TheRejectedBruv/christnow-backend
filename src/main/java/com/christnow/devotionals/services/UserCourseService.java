package com.christnow.devotionals.services;

import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.models.UserCourse;
import com.christnow.devotionals.repositories.CourseRepository;
import com.christnow.devotionals.repositories.UserCourseRepository;
import com.christnow.devotionals.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCourseService {
	  @Autowired
	    private UserCourseRepository userCourseRepository;

	    @Autowired
	    private UserRepository userRepository;

	    @Autowired
	    private CourseRepository courseRepository;

	    public List<UserCourse> getUserCourses(User user) {
	        return userCourseRepository.findByUser(user);
	    }

	    public boolean enrollUserInCourse(User user, Course course, boolean isFreeCourse) {
	        if (userCourseRepository.existsByUserAndCourse(user, course)) {
	            throw new RuntimeException("Already enrolled in this course");
	        }
	        
	        
		    if(isFreeCourse) {
		    	long freeCoursesCount = countFreeCoursesForUser(user);
		    	if(freeCoursesCount >= 3) {
		    		throw new RuntimeException("You have already used your 3 free courses.");
		    	}
		    }
		    

	        UserCourse userCourse = new UserCourse(user, course, isFreeCourse);
	        userCourseRepository.save(userCourse);
	        return true;
	    }
	    
	   

	    public boolean isUserEnrolledInCourse(User user, Course course) {
	        return userCourseRepository.existsByUserAndCourse(user, course);
	    }

	    public long countFreeCoursesForUser(User user) {
	    	return userCourseRepository.countByUserAndIsFreeCourseTrue(user);
	    }

	  
	    public boolean removeUserFromCourse(User user, Course course) {
	        return userCourseRepository.findByUserAndCourse(user, course).map(userCourse -> {
	            userCourseRepository.delete(userCourse);
	            return true;
	        }).orElse(false);
	    }
	    
	    public boolean userOwnsCourse(User user, Course course) {
	    	return userCourseRepository.existsByUserAndCourse(user, course);
	    }
}
