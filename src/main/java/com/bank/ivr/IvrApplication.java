package com.bank.ivr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the IVR system using Spring Boot.
 */
@SpringBootApplication
public class IvrApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(IvrApplication.class);
    
    /**
     * Entry point for the IVR application.
     * This starts the Spring Boot application.
     */
    public static void main(String[] args) {
        logger.info("Starting Bank IVR System with Spring Boot");
        SpringApplication.run(IvrApplication.class, args);
        logger.info("Bank IVR System started successfully");
    }
} 