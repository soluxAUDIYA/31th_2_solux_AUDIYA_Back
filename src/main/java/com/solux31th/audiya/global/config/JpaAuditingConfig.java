package com.solux31th.audiya.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// JPA Auditing 활성화. BaseTimeEntity의 createdAt, updatedAt이 자동으로 기록됨
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}