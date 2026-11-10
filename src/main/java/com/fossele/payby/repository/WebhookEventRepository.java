package com.fossele.payby.repository;

import com.fossele.payby.entity.WebhookEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEventLog, String> {
}
