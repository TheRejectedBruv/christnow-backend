package com.christnow.devotionals.models;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Lesson {
	

	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne
	    @JoinColumn(name = "course_id")
	    @JsonBackReference
	    private Course course;

	    private String title;
	    private String videoUrl;
	    private int lessonOrder;
	    private String content; // Optional: for text/quiz, etc.
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public Course getCourse() {
			return course;
		}
		public void setCourse(Course course) {
			this.course = course;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getVideoUrl() {
			return videoUrl;
		}
		public void setVideoUrl(String videoUrl) {
			this.videoUrl = videoUrl;
		}
		public int getLessonOrder() {
			return lessonOrder;
		}
		public void setLessonOrder(int lessonOrder) {
			this.lessonOrder = lessonOrder;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}

	    
}
