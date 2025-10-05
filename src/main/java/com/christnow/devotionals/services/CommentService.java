package com.christnow.devotionals.services;
import com.christnow.devotionals.models.Comment;
import com.christnow.devotionals.models.Devotional;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.repositories.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public List<Comment> getCommentsForDevotional(Devotional devotional) {
        return commentRepository.findByDevotional(devotional);
    }

    public Comment addComment(User user, Devotional devotional, String content) {
        Comment comment = new Comment(user, devotional, content);
        return commentRepository.save(comment);
    }
}
