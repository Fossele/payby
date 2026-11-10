package com.fossele.payby.controller;


import com.fossele.payby.entity.PaymentAuditLog;
import com.fossele.payby.entity.Subscription;
import com.fossele.payby.repository.PaymentAuditLogRepository;
import com.fossele.payby.repository.SubscriptionRepository;
import com.fossele.payby.service.StripeBillingService;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final StripeBillingService stripeBillingService;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentAuditLogRepository auditLogRepository;



    // 1. Details & Payment History
    @GetMapping("/customers/{customerId}/subscription")
    public ResponseEntity<Subscription> getSubscription(@PathVariable String customerId) {
        return subscriptionRepository.findByCustomerId(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customers/{customerId}/history")
    public ResponseEntity<List<PaymentAuditLog>> getPaymentHistory(@PathVariable String customerId) {
        return ResponseEntity.ok(auditLogRepository.findByCustomerIdOrderByCreatedAtDesc(customerId));
    }

    // 2. Create Payment Intent
    @PostMapping("/intents")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(
            @RequestParam String customerId,
            @RequestParam Long amount,
            @RequestParam String currency) throws Exception {
        PaymentIntent intent = stripeBillingService.createPaymentIntent(amount, currency, customerId);
        return ResponseEntity.ok(Map.of("clientSecret", intent.getClientSecret()));
    }

    // 3. Create/Subscribe Tier
    @PostMapping("/subscriptions")
    public ResponseEntity<Map<String, Object>> createSubscription(
            @RequestParam String customerId,
            @RequestParam String priceId) throws Exception {
        com.stripe.model.Subscription sub = stripeBillingService.createSubscription(customerId, priceId);
        return ResponseEntity.ok(Map.of("subscriptionId", sub.getId(), "status", sub.getStatus()));
    }

    // 4. Cancel / Downgrade Tier
    @DeleteMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<Void> cancelSubscription(
            @PathVariable String subscriptionId,
            @RequestParam(defaultValue = "false") boolean immediately) throws Exception {
        stripeBillingService.cancelSubscription(subscriptionId, immediately);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/subscriptions/{subscriptionId}/tier")
    public ResponseEntity<Void> updateTier(
            @PathVariable String subscriptionId,
            @RequestParam String newPriceId) throws Exception {
        stripeBillingService.updateSubscriptionTier(subscriptionId, newPriceId);
        return ResponseEntity.ok().build();
    }

    // 5. Update Default Payment Method
    @PutMapping("/subscriptions/{subscriptionId}/payment-method")
    public ResponseEntity<Void> updatePaymentMethod(
            @PathVariable String subscriptionId,
            @RequestParam String customerId,
            @RequestParam String paymentMethodId) throws Exception {
        stripeBillingService.updateDefaultPaymentMethod(customerId, subscriptionId, paymentMethodId);
        return ResponseEntity.ok().build();
    }
}