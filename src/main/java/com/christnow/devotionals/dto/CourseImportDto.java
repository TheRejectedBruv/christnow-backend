package com.christnow.devotionals.dto;

import java.util.ArrayList;
import java.util.List;

public class CourseImportDto {
    private String title;
    private String description;
    private double price;
    private boolean free;
    private String difficulty;
    private String imageUrl;
    private List<LessonImportDto> lessons = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isFree() { return free; }
    public void setFree(boolean free) { this.free = free; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<LessonImportDto> getLessons() { return lessons; }
    public void setLessons(List<LessonImportDto> lessons) { this.lessons = lessons; }
}
