package com.fitterapp.analytics.repository;

import com.fitterapp.analytics.entity.audit.AdminAuditLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, UUID> {}
