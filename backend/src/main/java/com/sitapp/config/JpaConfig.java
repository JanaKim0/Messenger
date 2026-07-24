package com.sitapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so {@code @CreatedDate} / {@code @LastModifiedDate}
 * timestamps are populated automatically.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
