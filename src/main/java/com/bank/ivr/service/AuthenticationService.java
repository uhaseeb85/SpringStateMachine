package com.bank.ivr.service;

/**
 * Service interface for handling user authentication methods.
 */
public interface AuthenticationService {
    
    /**
     * Authenticates a user by SSN
     * 
     * @param ssn The user's Social Security Number
     * @return True if authentication is successful, false otherwise
     */
    boolean authenticateBySSN(String ssn);
    
    /**
     * Authenticates a user by card number and PIN
     * 
     * @param cardNumber The user's debit card number
     * @param pin The user's PIN
     * @return True if authentication is successful, false otherwise
     */
    boolean authenticateByCardAndPin(String cardNumber, String pin);
    
    /**
     * Retrieves customer ID after successful authentication
     * 
     * @return The customer's ID if authenticated, or null otherwise
     */
    String getAuthenticatedCustomerId();
} 