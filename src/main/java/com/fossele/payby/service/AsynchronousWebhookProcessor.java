package com.fossele.payby.service;


import com.fossele.payby.entity.PaymentAuditLog;
import com.fossele.payby.entity.WebhookEventLog;
import com.fossele.payby.repository.PaymentAuditLogRepository;
import com.fossele.payby.repository.SubscriptionRepository;
import com.fossele.payby.repository.WebhookEventRepository;
import com.fossele.payby.state.SubscriptionState;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AsynchronousWebhookProcessor {

    private final WebhookEventRepository webhookEventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentAuditLogRepository auditLogRepository;


    @Async
    @Transactional
    public void processEventAsync(Event event) {
        // Step 1: Guarantee idempotency via unique DB index on event.getId()
        try {
            webhookEventRepository.saveAndFlush(WebhookEventLog.builder().eventId(event.getId()).type(event.getType()).status("PROCESSING").build());
        } catch (DataIntegrityViolationException e) {
            // Event already recorded; ignore duplicate payload safely
            return;
        }

        // Step 2: Route events to appropriate domain handlers
        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeObject == null) {
            updateEventStatus(event.getId(), "FAILED");
            return;
        }

        switch (event.getType()) {
            case "customer.subscription.created":
            case "customer.subscription.updated":
            case "customer.subscription.deleted":
                handleSubscriptionChange((Subscription) stripeObject);
                break;
            case "invoice.payment_succeeded":
                handleInvoicePaymentSucceeded((Invoice) stripeObject);
                break;
            case "invoice.payment_failed":
                handleInvoicePaymentFailed((Invoice) stripeObject);
                break;
            default:
                break;
        }

        updateEventStatus(event.getId(), "PROCESSED");
    }

    private void handleSubscriptionChange(Subscription stripeSub) {
        SubscriptionState newState = SubscriptionState.valueOf(stripeSub.getStatus().toUpperCase());

        subscriptionRepository.findById(stripeSub.getId()).ifPresentOrElse(sub -> {
            SubscriptionState currentState = SubscriptionState.valueOf(sub.getStatus());
            if (currentState.canTransitionTo(newState)) {
                sub.setStatus(newState.name());
                sub.setCancelAtPeriodEnd(stripeSub.getCancelAtPeriodEnd());
                sub.setCurrentPeriodStart(Instant.ofEpochSecond(stripeSub.getCurrentPeriodStart()));
                sub.setCurrentPeriodEnd(Instant.ofEpochSecond(stripeSub.getCurrentPeriodEnd()));
                subscriptionRepository.save(sub);
            }
        }, () -> {
            com.fossele.payby.entity.Subscription newSub = com.fossele.payby.entity.Subscription.builder()
                    .id(stripeSub.getId())
                    .customerId(stripeSub.getCustomer())
                    .status(newState.name())
                    .currentPeriodStart(Instant.ofEpochSecond(stripeSub.getCurrentPeriodStart()))
                    .currentPeriodEnd(Instant.ofEpochSecond(stripeSub.getCurrentPeriodEnd()))
                    .tier(stripeSub.getItems().getData().get(0).getPrice().getId())
                    .build();

            subscriptionRepository.save(newSub);
        });
    }

    private void handleInvoicePaymentSucceeded(Invoice invoice) {
        PaymentAuditLog log =  PaymentAuditLog.builder()
                .customerId(invoice.getCustomer())
                .invoiceId(invoice.getId())
                .paymentIntentId(invoice.getPaymentIntent())
                .amountCents(invoice.getAmountPaid())
                .currency(invoice.getCurrency())
                .status("SUCCEEDED")
                .build();
        auditLogRepository.save(log);
    }

    private void handleInvoicePaymentFailed(Invoice invoice) {
        PaymentAuditLog log = new PaymentAuditLog();
        log.setCustomerId(invoice.getCustomer());
        log.setInvoiceId(invoice.getId());
        log.setPaymentIntentId(invoice.getPaymentIntent());
        log.setAmountCents(invoice.getAmountDue());
        log.setCurrency(invoice.getCurrency());
        log.setStatus("FAILED");
        log.setFailureReason("Invoice payment attempt failed.");
        auditLogRepository.save(log);
    }

    private void updateEventStatus(String eventId, String status) {
        webhookEventRepository.findById(eventId).ifPresent(log -> {
            log.setStatus(status);
            webhookEventRepository.save(log);
        });
    }

}