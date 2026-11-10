package com.fossele.payby.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_audit_logs")
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    private String customerId;
    private String paymentIntentId;
    private String invoiceId;
    private Long amountCents;
    private String currency;
    private String status;
    private String failureReason;

    @CreationTimestamp
    private Instant createdAt ;

    // Getters, Setters, Constructors omitted for brevity
}
