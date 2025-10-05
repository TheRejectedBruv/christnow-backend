package com.christnow.devotionals;

import org.springframework.web.bind.annotation.RequestBody;
import java.time.LocalDateTime;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.christnow.devotionals.models.Devotional;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.repositories.DevotionalRepository;
import com.christnow.devotionals.repositories.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/devotionals")
public class DevotionController {
	
	@Autowired
	private DevotionalRepository devotionalRepository;
	
	@Autowired
	private UserRepository userRepository;
	
    @GetMapping
    public List<Devotional> getAllDevotionals() {
    	return devotionalRepository.findAll();
    }
	
		@PostMapping
		public Devotional createDevotional(@RequestBody Devotional devotional) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String username = authentication.getName();
			
			com.christnow.devotionals.models.User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("user not found"));
			devotional.setUser(user);
			return devotionalRepository.save(devotional);
		}
		@GetMapping("/{id}")
		public ResponseEntity<Devotional> getDevotionalById(@PathVariable Long id) {
			return devotionalRepository.findById(id)
					.map(ResponseEntity::ok)
					.orElse(ResponseEntity.notFound().build());
		}
		@PutMapping("/{id}") 
		public ResponseEntity<Devotional> updateDevotional(@PathVariable Long id, @RequestBody Devotional updatedDevotional) {
			return devotionalRepository.findById(id).map(devotional -> {
				updatedDevotional.setId(id);
				devotional.setTitle(updatedDevotional.getTitle());
				devotional.setVerse(updatedDevotional.getVerse());
				devotional.setMessage(updatedDevotional.getMessage());
				devotional.setUpdatedAt(LocalDateTime.now());
				return ResponseEntity.ok(devotionalRepository.save(devotional));
			}).orElse(ResponseEntity.notFound().build());
		}
		@DeleteMapping("/{id}")
		public ResponseEntity<?> deleteDevotional(@PathVariable Long id) {
			return devotionalRepository.findById(id)
					.map(devotional -> {
				devotionalRepository.delete(devotional);
				return ResponseEntity.noContent().build();
			}).orElse(ResponseEntity.notFound().build());
		}
		
		@GetMapping("/mine")
		public List<Devotional> getMyDevotionals() {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String username = authentication.getName();
			
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("User not found."));
			
			return devotionalRepository.findByUser(user);
		}
	
}
	

