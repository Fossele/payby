package com.fossele.payby.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.swing.plaf.BorderUIResource;
import java.time.Instant;

@Entity
@Table(name = "subscriptions")
@Setter
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Subscription {
    @Id
    private String id; // Stripe Subscription ID

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String status;

    private Instant currentPeriodStart;
    private Instant currentPeriodEnd;

    @Builder.Default
    private Boolean cancelAtPeriodEnd = false;

    @Column(nullable = false)
    private String tier;


    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private  Instant updatedAt;

    // Getters, Setters, Constructors omitted for brevity
}