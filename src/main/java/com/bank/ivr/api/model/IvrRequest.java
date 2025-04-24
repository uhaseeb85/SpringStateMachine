package com.bank.ivr.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Model class representing an IVR request from the IVP client.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IvrRequest {
    
    private String sessionId;
    private String userInput;
    private String inputType;
    
    // Default constructor for JSON serialization
    public IvrRequest() {
    }
    
    public IvrRequest(String sessionId, String userInput, String inputType) {
        this.sessionId = sessionId;
        this.userInput = userInput;
        this.inputType = inputType;
    }

    /**
     * Gets the unique session ID for this conversation
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the unique session ID for this conversation
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Gets the user input value (could be SSN, card number, PIN, menu selection, etc.)
     */
    public String getUserInput() {
        return userInput;
    }

    /**
     * Sets the user input value
     */
    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    /**
     * Gets the type of input being provided (e.g., "SSN", "CARD_NUMBER", "PIN", "MENU_SELECTION")
     */
    public String getInputType() {
        return inputType;
    }

    /**
     * Sets the type of input being provided
     */
    public void setInputType(String inputType) {
        this.inputType = inputType;
    }
    
    @Override
    public String toString() {
        return "IvrRequest{" +
                "sessionId='" + sessionId + '\'' +
                ", inputType='" + inputType + '\'' +
                ", userInput='[MASKED]'" +
                '}';
    }
} 