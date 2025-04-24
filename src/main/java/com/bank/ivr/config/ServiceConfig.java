package com.bank.ivr.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for service components.
 */
@Configuration
@ComponentScan(basePackages = {
    "com.bank.ivr.service", 
    "com.bank.ivr.action"
})
public class ServiceConfig {
    // Bean definitions can be added here if needed
} 