package com.christnow.devotionals.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Comment {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "devotional_id")
    private Devotional devotional;

    private String content;

    private LocalDateTime createdAt;

    public Comment() {}

    public Comment(User user, Devotional devotional, String content) {
        this.user = user;
        this.devotional = devotional;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public Devotional getDevotional() { return devotional; }

    public void setDevotional(Devotional devotional) { this.devotional = devotional; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
