package com.christnow.devotionals.controllers;

import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.models.PaymentRequest;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.repositories.CourseRepository;
import com.christnow.devotionals.repositories.UserRepository;
import com.christnow.devotionals.security.AuthUtils;
import com.christnow.devotionals.services.PaymentFulfillmentService;
import com.christnow.devotionals.services.StripeService;
import com.christnow.devotionals.services.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/payment", "/api/payment"})
public class PaymentController {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PaymentFulfillmentService paymentFulfillmentService;

    @Autowired
    private UserService userService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(
            @RequestBody PaymentRequest paymentRequest,
            Authentication authentication
    ) {
        if (!paymentFulfillmentService.isStripeConfigured()) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Stripe is not configured on the server. Set STRIPE_SECRET_KEY on Heroku.");
        }

        String userEmail = AuthUtils.resolveEmail(authentication);
        if (userEmail == null || userEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in required.");
        }

        User user = userRepository.findByEmailIgnoreCase(userEmail.trim()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
        }

        if (paymentRequest.getCourseId() == null) {
            return ResponseEntity.badRequest().body("courseId is required.");
        }

        Course course = courseRepository.findById(paymentRequest.getCourseId()).orElse(null);
        if (course == null) {
            return ResponseEntity.badRequest().body("Course not found.");
        }

        if (user.getOwnedCourses().stream().anyMatch(c -> course.getId().equals(c.getId()))
                || user.getFreeCourses().stream().anyMatch(c -> course.getId().equals(c.getId()))) {
            return ResponseEntity.badRequest().body("You already own this course.");
        }

        long expectedCents = Math.round(course.getPrice() * 100);
        if (expectedCents <= 0) {
            return ResponseEntity.badRequest().body("This course is not available for purchase.");
        }
        if (paymentRequest.getAmount() != expectedCents) {
            return ResponseEntity.badRequest().body("Invalid amount for this course.");
        }

        try {
            String url = stripeService.createCheckoutSession(
                    userEmail,
                    List.of(course.getTitle()),
                    course.getId(),
                    expectedCents,
                    paymentRequest.getSuccessUrl(),
                    paymentRequest.getCancelUrl()
            );
            return ResponseEntity.ok(Map.of("checkoutUrl", url));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Payment provider error: " + e.getMessage());
        }
    }

    @PostMapping({"/confirm-session", "/complete-purchase"})
    public ResponseEntity<?> completePurchase(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String userEmail = AuthUtils.resolveEmail(authentication);
        if (userEmail == null || userEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in required.");
        }

        if (!paymentFulfillmentService.isStripeConfigured()) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Stripe is not configured on the server. Set STRIPE_SECRET_KEY on Heroku.");
        }

        String sessionId = body.get("sessionId");
        if (sessionId == null || sessionId.isBlank()) {
            return ResponseEntity.badRequest().body("sessionId is required.");
        }

        Long fallbackCourseId = null;
        String courseIdRaw = body.get("courseId");
        if (courseIdRaw != null && !courseIdRaw.isBlank()) {
            try {
                fallbackCourseId = Long.parseLong(courseIdRaw.trim());
            } catch (NumberFormatException ignored) {
                return ResponseEntity.badRequest().body("Invalid courseId.");
            }
        }

        if (fallbackCourseId == null) {
            return ResponseEntity.badRequest().body("courseId is required.");
        }

        Course course = courseRepository.findById(fallbackCourseId).orElse(null);
        if (course == null) {
            return ResponseEntity.badRequest().body("Course not found for id " + fallbackCourseId + ".");
        }

        try {
            Session session = paymentFulfillmentService.retrieveSession(sessionId.trim());

            if (!paymentFulfillmentService.isSessionComplete(session)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        "Checkout session is not complete yet. status=" + session.getStatus()
                                + ", paymentStatus=" + session.getPaymentStatus()
                );
            }

            if (!paymentFulfillmentService.isAmountValidForCourse(session, course)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        "Payment amount does not match this course. expected="
                                + Math.round(course.getPrice() * 100)
                                + ", actual=" + session.getAmountTotal()
                );
            }

            userService.addOwnedCourseByEmail(userEmail.trim(), course.getId());

            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "courseId", String.valueOf(course.getId())
            ));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Payment provider error: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
