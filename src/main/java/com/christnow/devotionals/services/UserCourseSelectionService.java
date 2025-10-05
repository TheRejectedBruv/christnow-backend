package com.christnow.devotionals.services;

import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.models.UserCourseSelection;
import com.christnow.devotionals.repositories.UserCourseSelectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCourseSelectionService {
	   @Autowired
	    private UserCourseSelectionRepository userCourseSelectionRepository;

	    // Get all free courses selected by a user
	    public List<UserCourseSelection> getFreeCoursesForUser(User user) {
	        return userCourseSelectionRepository.findByUserAndFreeSelectionTrue(user);
	    }

	    // Check if user already picked this course
	    public boolean hasUserPickedCourse(User user, Course course) {
	        return userCourseSelectionRepository.existsByUserAndCourse(user, course);
	    }

	    // Save a new course selection
	    public UserCourseSelection saveCourseSelection(UserCourseSelection selection) {
	        return userCourseSelectionRepository.save(selection);
	    }

	    // Get how many free courses the user has picked so far
	    public int getFreeCourseCount(User user) {
	        return getFreeCoursesForUser(user).size();
	    }
}
