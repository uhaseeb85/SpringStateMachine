package com.bank.ivr.service.impl;

import com.bank.ivr.service.AuthenticationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the authentication service.
 * For demonstration purposes, this uses hardcoded values.
 * In a real application, this would connect to a database or external service.
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private static final Logger logger = LogManager.getLogger(AuthenticationServiceImpl.class);
    
    // For demonstration, we'll use these maps to simulate a database
    private final Map<String, String> ssnToCustomerId = new HashMap<>();
    private final Map<String, String> cardToPinMap = new HashMap<>();
    private final Map<String, String> cardToCustomerId = new HashMap<>();
    
    private String currentCustomerId;
    
    public AuthenticationServiceImpl() {
        logger.info("Initializing Authentication Service with demo data");
        // Initialize with some demo data
        ssnToCustomerId.put("123-45-6789", "CUST001");
        ssnToCustomerId.put("987-65-4321", "CUST002");
        
        cardToPinMap.put("4111111111111111", "1234");
        cardToPinMap.put("5555555555554444", "5678");
        
        cardToCustomerId.put("4111111111111111", "CUST001");
        cardToCustomerId.put("5555555555554444", "CUST002");
    }
    
    @Override
    public boolean authenticateBySSN(String ssn) {
        logger.debug("Attempting SSN authentication");
        
        // Basic format validation
        if (ssn == null || ssn.trim().isEmpty()) {
            logger.debug("SSN authentication failed: SSN is null or empty");
            return false;
        }
        
        // Remove any hyphens and whitespace
        ssn = ssn.replaceAll("[\\s-]", "");
        
        // Check length
        if (ssn.length() != 9) {
            logger.debug("SSN authentication failed: Invalid length");
            return false;
        }
        
        // Check if all characters are digits
        if (!ssn.matches("\\d{9}")) {
            logger.debug("SSN authentication failed: Contains non-digit characters");
            return false;
        }
        
        // Check for invalid patterns
        if (ssn.matches("000.*") || ssn.matches("666.*") || 
            ssn.matches("9.*") || ssn.equals("123456789")) {
            logger.debug("SSN authentication failed: Invalid pattern");
            return false;
        }
        
        // Format SSN with hyphens for lookup
        String formattedSsn = String.format("%s-%s-%s", 
            ssn.substring(0, 3), 
            ssn.substring(3, 5), 
            ssn.substring(5, 9));
        
        // Check against stored SSNs
        if (ssnToCustomerId.containsKey(formattedSsn)) {
            currentCustomerId = ssnToCustomerId.get(formattedSsn);
            logger.debug("SSN authentication successful for customer {}", currentCustomerId);
            return true;
        }
        
        logger.debug("SSN authentication failed: SSN not found in database");
        return false;
    }
    
    @Override
    public boolean authenticateByCardAndPin(String cardNumber, String pin) {
        logger.debug("Attempting card/PIN authentication");
        if (cardToPinMap.containsKey(cardNumber) && 
            cardToPinMap.get(cardNumber).equals(pin)) {
            currentCustomerId = cardToCustomerId.get(cardNumber);
            logger.debug("Card/PIN authentication successful for customer {}", currentCustomerId);
            return true;
        }
        logger.debug("Card/PIN authentication failed");
        return false;
    }
    
    @Override
    public String getAuthenticatedCustomerId() {
        return currentCustomerId;
    }
} 