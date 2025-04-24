package com.bank.ivr.model;

/**
 * Enum representing all possible states in the IVR system.
 */
public enum IvrState {
    WELCOME,               // Initial greeting state
    AUTHENTICATION_METHOD, // Choose authentication method
    SSN_PROMPT,            // Prompt for SSN
    CARD_NUMBER_PROMPT,    // Prompt for debit card number
    PIN_PROMPT,            // Prompt for PIN
    VALIDATING,            // Validating provided credentials
    AUTHENTICATED,         // Successfully authenticated
    MAIN_MENU,             // Main menu after authentication
    ACCOUNT_SERVICES,      // Account services submenu
    TRANSFER_FUNDS,        // Transfer funds option
    BALANCE_INQUIRY,       // Balance inquiry option
    TRANSACTION_HISTORY,   // Transaction history option
    ERROR,                 // Error state
    END_CALL               // End of call
} 