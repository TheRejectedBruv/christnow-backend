package com.christnow.devotionals.controllers;

import com.christnow.devotionals.services.PaymentFulfillmentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/payment", "/api/payment"})
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Autowired
    private PaymentFulfillmentService paymentFulfillmentService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            try {
                Session session = paymentFulfillmentService.sessionFromWebhookEvent(event);
                paymentFulfillmentService.fulfillCheckoutSession(session);
            } catch (StripeException e) {
                System.out.println("Webhook session retrieval failed: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Stripe session retrieval failed");
            }
        }

        return ResponseEntity.ok("Webhook handled");
    }
}
