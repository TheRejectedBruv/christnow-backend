package com.christnow.devotionals.services;

import com.christnow.devotionals.models.Course;
import com.christnow.devotionals.models.User;
import com.christnow.devotionals.repositories.CourseRepository;
import com.christnow.devotionals.repositories.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PaymentFulfillmentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserService userService;

    @Value("${stripe.secret.key:}")
    private String stripeSecretKey;

    public boolean fulfillCheckoutSession(Session session) {
        return fulfillCheckoutSessionForUser(session, resolveCustomerEmail(session));
    }

    public boolean fulfillCheckoutSessionForUser(Session session, String userEmail) {
        return fulfillCheckoutSessionForUser(session, userEmail, null);
    }

    @Transactional
    public boolean fulfillCheckoutSessionForUser(Session session, String userEmail, Long fallbackCourseId) {
        if (session == null) {
            return false;
        }

        if (!isSessionComplete(session)) {
            System.out.println("Payment fulfillment skipped: session not complete. status="
                    + session.getStatus() + " paymentStatus=" + session.getPaymentStatus());
            return false;
        }

        if (userEmail == null || userEmail.isBlank()) {
            userEmail = resolveCustomerEmail(session);
        }
        if (userEmail == null || userEmail.isBlank()) {
            System.out.println("Payment fulfillment skipped: missing customer email.");
            return false;
        }

        Course course = resolveCourse(session, fallbackCourseId);
        if (course == null) {
            System.out.println("Payment fulfillment skipped: course not found for session " + session.getId());
            return false;
        }

        if (!isAmountValidForCourse(session, course)) {
            System.out.println("Payment fulfillment skipped: amount mismatch for course " + course.getId());
            return false;
        }

        userService.addOwnedCourseByEmail(userEmail.trim(), course.getId());
        return true;
    }

    public Session retrieveSession(String sessionId) throws StripeException {
        ensureStripeConfigured();
        return Session.retrieve(sessionId);
    }

    public void ensureStripeConfigured() {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new IllegalStateException(
                    "Stripe secret key is missing on the server. Set STRIPE_SECRET_KEY on Heroku."
            );
        }
        Stripe.apiKey = stripeSecretKey;
    }

    public boolean isStripeConfigured() {
        return stripeSecretKey != null && !stripeSecretKey.isBlank();
    }

    public Session sessionFromWebhookEvent(com.stripe.model.Event event) throws StripeException {
        ensureStripeConfigured();
        var deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            Object obj = deserializer.getObject().get();
            if (obj instanceof Session session) {
                return session;
            }
        }

        String rawJson = deserializer.getRawJson();
        if (rawJson != null && !rawJson.isBlank()) {
            String sessionId = extractSessionId(rawJson);
            if (sessionId != null) {
                return Session.retrieve(sessionId);
            }
        }

        return null;
    }

    private String extractSessionId(String rawJson) {
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*\"(cs_[^\"]+)\"").matcher(rawJson);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public boolean isSessionComplete(Session session) {
        if (session == null) {
            return false;
        }
        String status = session.getStatus();
        if ("complete".equalsIgnoreCase(status)) {
            return true;
        }
        if ("paid".equalsIgnoreCase(session.getPaymentStatus())) {
            return true;
        }
        return false;
    }

    public boolean isAmountValidForCourse(Session session, Course course) {
        if (session.getAmountTotal() == null) {
            return true;
        }
        long expected = Math.round(course.getPrice() * 100);
        return session.getAmountTotal() == expected;
    }

    private String resolveCustomerEmail(Session session) {
        if (session.getCustomerEmail() != null && !session.getCustomerEmail().isBlank()) {
            return session.getCustomerEmail();
        }
        if (session.getCustomerDetails() != null && session.getCustomerDetails().getEmail() != null) {
            return session.getCustomerDetails().getEmail();
        }
        return null;
    }

    private Course resolveCourse(Session session, Long fallbackCourseId) {
        if (session.getMetadata() != null) {
            String courseIdStr = session.getMetadata().get("courseId");
            String courseName = session.getMetadata().get("courseName");

            if (courseIdStr != null && !courseIdStr.isBlank()) {
                try {
                    Course byId = courseRepository.findById(Long.parseLong(courseIdStr)).orElse(null);
                    if (byId != null) {
                        return byId;
                    }
                } catch (NumberFormatException ignored) {
                    // fall through
                }
            }

            if (courseName != null && !courseName.isBlank()) {
                Course byTitle = courseRepository.findByTitle(courseName).orElse(null);
                if (byTitle != null) {
                    return byTitle;
                }
            }
        }

        if (fallbackCourseId != null) {
            return courseRepository.findById(fallbackCourseId).orElse(null);
        }

        return null;
    }
}
