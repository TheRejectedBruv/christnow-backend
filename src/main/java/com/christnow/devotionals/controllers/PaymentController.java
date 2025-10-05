package com.christnow.devotionals.controllers;

import com.christnow.devotionals.models.PaymentRequest;
import com.christnow.devotionals.services.StripeService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession(
            @RequestBody PaymentRequest paymentRequest,
            Principal principal
    ) throws StripeException {

        // 👤 Use logged-in user’s email from Spring Security
        String userEmail = principal.getName();

        String url = stripeService.createCheckoutSession(
                userEmail,
                paymentRequest.getCourseNames(),
                paymentRequest.getAmount(),
                paymentRequest.getSuccessUrl(),
                paymentRequest.getCancelUrl()
        );

        return Map.of("checkoutUrl", url);
    }
}
