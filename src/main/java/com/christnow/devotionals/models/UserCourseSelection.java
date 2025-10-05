package com.christnow.devotionals.models;

import jakarta.persistence.*;

@Entity
public class UserCourseSelection {
	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne
	    private User user;

	    @ManyToOne
	    private Course course;

	    private boolean freeSelection; // true if chosen as one of the free 3

	    public Long getId() { return id; }
	    public void setId(Long id) { this.id = id; }

	    public User getUser() { return user; }
	    public void setUser(User user) { this.user = user; }

	    public Course getCourse() { return course; }
	    public void setCourse(Course course) { this.course = course; }

	    public boolean isFreeSelection() { return freeSelection; }
	    public void setFreeSelection(boolean freeSelection) { this.freeSelection = freeSelection; }
}
