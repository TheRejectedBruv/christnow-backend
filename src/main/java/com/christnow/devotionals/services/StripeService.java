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

    /**
     * Creates a Stripe Checkout session
     *
     * @param customerEmail the email of the logged-in user
     * @param courseNames   names of courses being purchased
     * @param amount        amount in cents (e.g. $10 = 1000)
     * @param successUrl    redirect after success
     * @param cancelUrl     redirect if user cancels
     * @return checkout session URL
     */
    public String createCheckoutSession(
            String customerEmail,
            List<String> courseNames,
            long amount,
            String successUrl,
            String cancelUrl
    ) throws StripeException {

        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(amount)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(String.join(", ", courseNames))
                                                .build()
                                )
                                .build()
                )
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setCustomerEmail(customerEmail) // ✅ capture user email
                .putMetadata("courseName", String.join(", ", courseNames)) // ✅ save courseName
                .addLineItem(lineItem)
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}
