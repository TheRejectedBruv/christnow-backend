package com.christnow.devotionals.dto;

public class LessonImportDto {
    private String title;
    private String videoUrl;
    private int lessonOrder;
    private String content;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public int getLessonOrder() { return lessonOrder; }
    public void setLessonOrder(int lessonOrder) { this.lessonOrder = lessonOrder; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
