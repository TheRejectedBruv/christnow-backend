package com.christnow.devotionals.models;

import com.christnow.devotionals.models.Devotional;

import jakarta.persistence.*;

@Entity
public class Favorite {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@ManyToOne
	@JoinColumn(name = "devotional_id")
	private Devotional devotional;
	
	public Favorite() {}
	
	public Favorite(User user, Devotional devotional) {
		this.user = user;
		this.devotional = devotional;
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
	public Devotional getDevotional() {
		return devotional;
	}
	public void setDevotional(Devotional devotional) {
		this.devotional = devotional;
	}
}
