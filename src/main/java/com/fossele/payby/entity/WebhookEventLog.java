package com.fossele.payby.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "webhook_events")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class WebhookEventLog {
    @Id
    private String eventId; // Stripe Event ID

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String status;

    @Builder.Default
    private Instant processedAt = Instant.now();
}

