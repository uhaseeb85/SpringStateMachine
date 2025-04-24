package com.bank.ivr.api.controller;

import com.bank.ivr.api.model.IvrRequest;
import com.bank.ivr.api.model.IvrResponse;
import com.bank.ivr.service.IvrSessionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the IVR API.
 */
@RestController
@RequestMapping("/api/ivr")
public class IvrController {
    
    private static final Logger logger = LogManager.getLogger(IvrController.class);
    
    private final IvrSessionService sessionService;
    
    @Autowired
    public IvrController(IvrSessionService sessionService) {
        this.sessionService = sessionService;
    }
    
    /**
     * Initializes a new IVR session.
     * 
     * @return The initial IVR response
     */
    @PostMapping("/session")
    public ResponseEntity<IvrResponse> initializeSession() {
        logger.info("Received request to initialize new IVR session");
        
        IvrResponse response = sessionService.initializeSession(null);
        
        logger.info("Initialized session: {}", response.getSessionId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Processes a user input and advances the state machine.
     * 
     * @param request The IVR request containing session ID and user input
     * @return The next IVR response based on the new state
     */
    @PostMapping("/process")
    public ResponseEntity<IvrResponse> processUserInput(@RequestBody IvrRequest request) {
        String sessionId = request.getSessionId();
        logger.info("Received user input for session {}: {}", sessionId, request.getInputType());
        
        if (sessionId == null || sessionId.isEmpty()) {
            logger.warn("Request missing session ID");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(IvrResponse.builder()
                            .withErrorMessage("Session ID is required")
                            .build());
        }
        
        if (!sessionService.sessionExists(sessionId)) {
            logger.warn("Session not found: {}", sessionId);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(IvrResponse.builder()
                            .withSessionId(sessionId)
                            .withErrorMessage("Session not found")
                            .build());
        }
        
        IvrResponse response = sessionService.processUserInput(request);
        
        if (response.isCallEnded()) {
            logger.info("Call ended for session {}", sessionId);
            sessionService.endSession(sessionId);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Ends an IVR session.
     * 
     * @param sessionId The session ID to end
     * @return A success response
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<IvrResponse> endSession(@PathVariable String sessionId) {
        logger.info("Received request to end session: {}", sessionId);
        
        if (!sessionService.sessionExists(sessionId)) {
            logger.warn("Session not found: {}", sessionId);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(IvrResponse.builder()
                            .withSessionId(sessionId)
                            .withErrorMessage("Session not found")
                            .build());
        }
        
        sessionService.endSession(sessionId);
        
        IvrResponse response = IvrResponse.builder()
                .withSessionId(sessionId)
                .withNextAction("END_CALL")
                .withPromptMessage("Session ended")
                .withCallEnded(true)
                .build();
        
        return ResponseEntity.ok(response);
    }
} 