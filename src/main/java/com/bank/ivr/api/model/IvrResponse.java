package com.bank.ivr.api.model;

import com.bank.ivr.model.IvrState;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Model class representing an IVR response to the IVP client.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IvrResponse {
    
    private String sessionId;
    private IvrState currentState;
    private String nextAction;
    private String promptMessage;
    private boolean authenticated;
    private boolean callEnded;
    private String errorMessage;
    
    // Default constructor for JSON serialization
    public IvrResponse() {
    }

    /**
     * Gets the session ID for this conversation
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the session ID for this conversation
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Gets the current state of the IVR state machine
     */
    public IvrState getCurrentState() {
        return currentState;
    }

    /**
     * Sets the current state of the IVR state machine
     */
    public void setCurrentState(IvrState currentState) {
        this.currentState = currentState;
    }

    /**
     * Gets the next action the IVP client should take
     * (e.g., "COLLECT_SSN", "COLLECT_CARD", "COLLECT_PIN", "PRESENT_MENU")
     */
    public String getNextAction() {
        return nextAction;
    }

    /**
     * Sets the next action the IVP client should take
     */
    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

    /**
     * Gets the message to prompt the user with
     */
    public String getPromptMessage() {
        return promptMessage;
    }

    /**
     * Sets the message to prompt the user with
     */
    public void setPromptMessage(String promptMessage) {
        this.promptMessage = promptMessage;
    }

    /**
     * Gets whether the user is authenticated
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Sets whether the user is authenticated
     */
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    /**
     * Gets whether the call has ended
     */
    public boolean isCallEnded() {
        return callEnded;
    }

    /**
     * Sets whether the call has ended
     */
    public void setCallEnded(boolean callEnded) {
        this.callEnded = callEnded;
    }

    /**
     * Gets the error message, if any
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    /**
     * Builder for creating IvrResponse instances
     */
    public static class Builder {
        private String sessionId;
        private IvrState currentState;
        private String nextAction;
        private String promptMessage;
        private boolean authenticated;
        private boolean callEnded;
        private String errorMessage;
        
        public Builder withSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public Builder withCurrentState(IvrState currentState) {
            this.currentState = currentState;
            return this;
        }
        
        public Builder withNextAction(String nextAction) {
            this.nextAction = nextAction;
            return this;
        }
        
        public Builder withPromptMessage(String promptMessage) {
            this.promptMessage = promptMessage;
            return this;
        }
        
        public Builder withAuthenticated(boolean authenticated) {
            this.authenticated = authenticated;
            return this;
        }
        
        public Builder withCallEnded(boolean callEnded) {
            this.callEnded = callEnded;
            return this;
        }
        
        public Builder withErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public IvrResponse build() {
            IvrResponse response = new IvrResponse();
            response.sessionId = this.sessionId;
            response.currentState = this.currentState;
            response.nextAction = this.nextAction;
            response.promptMessage = this.promptMessage;
            response.authenticated = this.authenticated;
            response.callEnded = this.callEnded;
            response.errorMessage = this.errorMessage;
            return response;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
} 