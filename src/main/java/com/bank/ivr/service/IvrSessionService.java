package com.bank.ivr.service;

import com.bank.ivr.api.model.IvrRequest;
import com.bank.ivr.api.model.IvrResponse;
import com.bank.ivr.model.IvrState;

/**
 * Service interface for managing IVR sessions and state machine interactions.
 */
public interface IvrSessionService {
    
    /**
     * Initializes a new session, creating a state machine.
     * 
     * @param sessionId The unique session ID
     * @return The initial IVR response
     */
    IvrResponse initializeSession(String sessionId);
    
    /**
     * Processes a user input and advances the state machine.
     * 
     * @param request The IVR request containing session ID and user input
     * @return The next IVR response based on the new state
     */
    IvrResponse processUserInput(IvrRequest request);
    
    /**
     * Gets the current state for a given session.
     * 
     * @param sessionId The session ID
     * @return The current state, or null if session doesn't exist
     */
    IvrState getCurrentState(String sessionId);
    
    /**
     * Checks if a session exists.
     * 
     * @param sessionId The session ID
     * @return True if session exists, false otherwise
     */
    boolean sessionExists(String sessionId);
    
    /**
     * Ends a session, cleaning up any resources.
     * 
     * @param sessionId The session ID
     */
    void endSession(String sessionId);
} 