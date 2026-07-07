package com.christnow.devotionals.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public String createCheckoutSession(
            String customerEmail,
            List<String> courseNames,
            Long courseId,
            long amount,
            String successUrl,
            String cancelUrl
    ) throws StripeException {

        Stripe.apiKey = stripeSecretKey;

        String courseTitle = String.join(", ", courseNames);

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(amount)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(courseTitle)
                                                .build()
                                )
                                .build()
                )
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setCustomerEmail(customerEmail)
                .putMetadata("courseId", String.valueOf(courseId))
                .putMetadata("courseName", courseTitle)
                .putMetadata("userEmail", customerEmail)
                .addLineItem(lineItem)
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}
