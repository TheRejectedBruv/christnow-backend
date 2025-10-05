package com.christnow.devotionals.controllers;

import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.repositories.CourseRepository;
import com.christnow.devotionals.repositories.UserRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            // ✅ Verify the event came from Stripe
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("⚠️ Invalid signature");
        }

        // 🎯 React to checkout success
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .map(obj -> (Session) obj)
                    .orElse(null);

            if (session != null) {
                String customerEmail = session.getCustomerEmail(); // ✅ Email used in checkout
                String courseName = session.getMetadata().get("courseName"); // ✅ From metadata

                if (customerEmail != null && courseName != null) {
                    // 1. Find the user by email
                    User user = userRepository.findByEmail(customerEmail)
                            .orElse(null);

                    if (user != null) {
                        // 2. Find the course by name
                        Course course = courseRepository.findByTitle(courseName)
                                .orElse(null);

                        if (course != null) {
                            // 3. Add to owned courses
                            user.getOwnedCourses().add(course);
                            userRepository.save(user);

                            System.out.println("✅ User " + customerEmail + " purchased course: " + courseName);
                        } else {
                            System.out.println("⚠️ Course not found: " + courseName);
                        }
                    } else {
                        System.out.println("⚠️ User not found: " + customerEmail);
                    }
                } else {
                    System.out.println("⚠️ Missing email or course metadata in session");
                }
            }
        }

        return ResponseEntity.ok("Webhook handled");
    }
}
