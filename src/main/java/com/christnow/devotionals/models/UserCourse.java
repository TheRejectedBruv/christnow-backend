package com.christnow.devotionals.models;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class UserCourse {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne(optional = false)
	    @JoinColumn(name = "user_id", nullable = false)
	    private User user;

	    @ManyToOne(optional = false)
	    @JoinColumn(name = "course_id", nullable = false)
	    private Course course;
	    
	    private LocalDateTime enrolledAt;
	    
	    @PrePersist
	    protected void onEnroll() {
	    	this.enrolledAt = LocalDateTime.now();
	    }
	    
	    public LocalDateTime getEnrolledAt() {
	    	return enrolledAt;
	    }

	    private boolean isFreeCourse; // Tracks if it's one of the user's 3 free courses

	    public UserCourse() {}

	    public UserCourse(User user, Course course, boolean isFreeCourse) {
	        this.user = user;
	        this.course = course;
	        this.isFreeCourse = isFreeCourse;
	    }
	    
	    @Override
	    public String toString() {
	    	return "UserCourse{" +
	               "id=" + id +
	               ", user=" + user.getUsername() +
	               ", course=" + course.getTitle() +
	               ", isFreeCourse=" + isFreeCourse + 
	               ", enrolledAt=" + enrolledAt +
	               '}';
	    }

	    public Long getId() {
	        return id;
	    }

	    public User getUser() {
	        return user;
	    }

	    public void setUser(User user) {
	        this.user = user;
	    }

	    public Course getCourse() {
	        return course;
	    }

	    public void setCourse(Course course) {
	        this.course = course;
	    }

	    public boolean isFreeCourse() {
	        return isFreeCourse;
	    }

	    public void setFreeCourse(boolean freeCourse) {
	        isFreeCourse = freeCourse;
	    }
}
