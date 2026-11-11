package com.fossele.payby.service;


import com.fossele.payby.entity.WebhookEventLog;
import com.fossele.payby.repository.PaymentAuditLogRepository;
import com.fossele.payby.repository.SubscriptionRepository;
import com.fossele.payby.repository.WebhookEventRepository;
import com.stripe.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AsynchronousWebhookProcessorTest {

    private WebhookEventRepository webhookEventRepository;
    private SubscriptionRepository subscriptionRepository;
    private PaymentAuditLogRepository auditLogRepository;
    private AsynchronousWebhookProcessor webhookProcessor;

    @BeforeEach
    void setUp() {
        webhookEventRepository = mock(WebhookEventRepository.class);
        subscriptionRepository = mock(SubscriptionRepository.class);
        auditLogRepository = mock(PaymentAuditLogRepository.class);

        webhookProcessor = new AsynchronousWebhookProcessor(
                webhookEventRepository,
                subscriptionRepository,
                auditLogRepository
        );
    }

    @Test
    void testIdempotencyDuplicateEventIgnored() {
        Event event = new Event();
        event.setId("evt_duplicate_123");
        event.setType("customer.subscription.created");

        // Simulate unique constraint violation in PostgreSQL (event ID already processed)
        when(webhookEventRepository.saveAndFlush(any(WebhookEventLog.class)))
                .thenThrow(new DataIntegrityViolationException("Unique key violation"));

        webhookProcessor.processEventAsync(event);

        // Verify that registration was attempted, but subscription lookup/creation never executed
        verify(webhookEventRepository, times(1)).saveAndFlush(any(WebhookEventLog.class));
        verify(subscriptionRepository, never()).findById(anyString());
    }
}