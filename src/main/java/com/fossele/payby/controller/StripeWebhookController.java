package com.fossele.payby.controller;


import com.fossele.payby.service.AsynchronousWebhookProcessor;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final AsynchronousWebhookProcessor asyncProcessor;



    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        // Delegate execution asynchronously; reply 200 OK immediately to Stripe
        asyncProcessor.processEventAsync(event);

        return ResponseEntity.ok("Received");
    }
}