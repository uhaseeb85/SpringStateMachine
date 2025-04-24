package com.bank.ivr.model;

/**
 * Enum representing all possible events in the IVR system.
 */
public enum IvrEvent {
    CALL_CONNECTED,          // Initial call connection
    SELECT_SSN_AUTH,         // User selects SSN authentication
    SELECT_CARD_AUTH,        // User selects card authentication
    ENTER_SSN,               // User enters SSN
    ENTER_CARD_NUMBER,       // User enters card number
    ENTER_PIN,               // User enters PIN
    AUTHENTICATION_SUCCESS,  // Authentication successful
    AUTHENTICATION_FAILURE,  // Authentication failed
    SELECT_ACCOUNT_SERVICES, // User selects account services
    SELECT_TRANSFER_FUNDS,   // User selects transfer funds
    SELECT_BALANCE_INQUIRY,  // User selects balance inquiry
    SELECT_TRANSACTION_HISTORY, // User selects transaction history
    COMPLETE_TRANSACTION,    // Transaction completed
    ERROR_OCCURRED,          // Error occurred
    TIMEOUT,                 // User timeout
    BACK,                    // Go back to previous menu
    END_CALL                 // User ends call
} 