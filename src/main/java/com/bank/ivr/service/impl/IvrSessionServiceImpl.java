package com.bank.ivr.service.impl;

import com.bank.ivr.api.model.IvrRequest;
import com.bank.ivr.api.model.IvrResponse;
import com.bank.ivr.model.CustomerCredentials;
import com.bank.ivr.model.IvrEvent;
import com.bank.ivr.model.IvrState;
import com.bank.ivr.service.IvrSessionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the IVR session service.
 */
@Service
public class IvrSessionServiceImpl implements IvrSessionService {
    
    private static final Logger logger = LogManager.getLogger(IvrSessionServiceImpl.class);
    
    private final StateMachineFactory<IvrState, IvrEvent> stateMachineFactory;
    private final Map<String, StateMachine<IvrState, IvrEvent>> sessions = new ConcurrentHashMap<>();
    
    @Autowired
    public IvrSessionServiceImpl(StateMachineFactory<IvrState, IvrEvent> stateMachineFactory) {
        this.stateMachineFactory = stateMachineFactory;
    }
    
    @Override
    public IvrResponse initializeSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = generateSessionId();
        }
        
        logger.info("Initializing new IVR session: {}", sessionId);
        
        StateMachine<IvrState, IvrEvent> machine = stateMachineFactory.getStateMachine();
        machine.start();
        
        // Add custom listeners
        addStateListeners(machine);
        
        sessions.put(sessionId, machine);
        
        // Trigger initial event to move from WELCOME to AUTHENTICATION_METHOD
        machine.sendEvent(IvrEvent.CALL_CONNECTED);
        
        return createResponseForCurrentState(sessionId, machine.getState().getId());
    }
    
    @Override
    public IvrResponse processUserInput(IvrRequest request) {
        String sessionId = request.getSessionId();
        if (!sessionExists(sessionId)) {
            logger.warn("Session not found: {}", sessionId);
            return IvrResponse.builder()
                    .withSessionId(sessionId)
                    .withErrorMessage("Session not found")
                    .build();
        }
        
        StateMachine<IvrState, IvrEvent> machine = sessions.get(sessionId);
        IvrState currentState = machine.getState().getId();
        String userInput = request.getUserInput();
        String inputType = request.getInputType();
        
        logger.info("Processing input for session {}: state={}, inputType={}", 
                sessionId, currentState, inputType);
        
        // Process the input based on the current state
        try {
            processStateWithInput(machine, currentState, userInput, inputType);
            
            // Get the new state after processing
            IvrState newState = machine.getState().getId();
            logger.info("State transition: {} -> {}", currentState, newState);
            
            // If we're in VALIDATING state, wait a moment for the validation events to be processed
            if (newState == IvrState.VALIDATING) {
                // Wait a short time for transitions to complete
                try {
                    logger.info("In VALIDATING state, waiting for events to be processed...");
                    
                    // Try multiple times with increasing waits
                    for (int attempt = 1; attempt <= 3; attempt++) {
                        // Wait with increasing times
                        Thread.sleep(attempt * 200);
                        
                        // Check if we're still in VALIDATING
                        newState = machine.getState().getId();
                        logger.info("After waiting attempt {}, state is now: {}", attempt, newState);
                        
                        // If we've transitioned out of VALIDATING, break the loop
                        if (newState != IvrState.VALIDATING) {
                            break;
                        }
                    }
                    
                    // If still in VALIDATING state after all attempts, force a transition
                    if (newState == IvrState.VALIDATING) {
                        logger.warn("Still in VALIDATING state after multiple attempts - forcing a transition based on credentials");
                        
                        // Check credentials to determine the appropriate transition
                        CustomerCredentials credentials = machine.getExtendedState().get("credentials", CustomerCredentials.class);
                        if (credentials != null && credentials.isAuthenticated()) {
                            logger.info("Forcing transition to AUTHENTICATED based on credential status");
                            machine.sendEvent(IvrEvent.AUTHENTICATION_SUCCESS);
                        } else {
                            logger.info("Forcing transition to ERROR based on credential status");
                            machine.sendEvent(IvrEvent.AUTHENTICATION_FAILURE);
                        }
                        
                        // One final check of the state
                        newState = machine.getState().getId();
                        logger.info("Final state after forced transition: {}", newState);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Interrupted while waiting for validation to complete", e);
                }
            }
            
            // Create response based on new state
            return createResponseForCurrentState(sessionId, newState);
            
        } catch (Exception e) {
            logger.error("Error processing user input", e);
            return IvrResponse.builder()
                    .withSessionId(sessionId)
                    .withCurrentState(currentState)
                    .withErrorMessage("Error processing input: " + e.getMessage())
                    .build();
        }
    }
    
    @Override
    public IvrState getCurrentState(String sessionId) {
        if (sessionExists(sessionId)) {
            return sessions.get(sessionId).getState().getId();
        }
        return null;
    }
    
    @Override
    public boolean sessionExists(String sessionId) {
        return sessions.containsKey(sessionId);
    }
    
    @Override
    public void endSession(String sessionId) {
        if (sessionExists(sessionId)) {
            logger.info("Ending IVR session: {}", sessionId);
            StateMachine<IvrState, IvrEvent> machine = sessions.get(sessionId);
            machine.stop();
            sessions.remove(sessionId);
        }
    }
    
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Process the current state with the given input and transition the state machine
     */
    private void processStateWithInput(StateMachine<IvrState, IvrEvent> machine, 
                                       IvrState currentState, 
                                       String userInput, 
                                       String inputType) {
        switch (currentState) {
            case AUTHENTICATION_METHOD:
                processAuthenticationMethodSelection(machine, userInput);
                break;
                
            case SSN_PROMPT:
                processSsnInput(machine, userInput);
                break;
                
            case CARD_NUMBER_PROMPT:
                processCardNumberInput(machine, userInput);
                break;
                
            case PIN_PROMPT:
                processPinInput(machine, userInput);
                break;
                
            case ERROR:
                processErrorStateInput(machine, userInput);
                break;
                
            case MAIN_MENU:
                processMainMenuSelection(machine, userInput);
                break;
                
            case ACCOUNT_SERVICES:
                processAccountServicesSelection(machine, userInput);
                break;
                
            case BALANCE_INQUIRY:
            case TRANSACTION_HISTORY:
            case TRANSFER_FUNDS:
                machine.sendEvent(IvrEvent.COMPLETE_TRANSACTION);
                break;
                
            default:
                logger.warn("Unhandled state in processStateWithInput: {}", currentState);
                break;
        }
    }
    
    private void processAuthenticationMethodSelection(StateMachine<IvrState, IvrEvent> machine, String selection) {
        if ("1".equals(selection)) {
            machine.sendEvent(IvrEvent.SELECT_SSN_AUTH);
        } else if ("2".equals(selection)) {
            machine.sendEvent(IvrEvent.SELECT_CARD_AUTH);
        } else if ("0".equals(selection)) {
            machine.sendEvent(IvrEvent.END_CALL);
        } else {
            logger.warn("Invalid authentication method selection: {}", selection);
        }
    }
    
    private void processSsnInput(StateMachine<IvrState, IvrEvent> machine, String ssn) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("ssn", ssn);
        machine.sendEvent(MessageBuilder
                .withPayload(IvrEvent.ENTER_SSN)
                .copyHeaders(headers)
                .build());
    }
    
    private void processCardNumberInput(StateMachine<IvrState, IvrEvent> machine, String cardNumber) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("cardNumber", cardNumber);
        machine.sendEvent(MessageBuilder
                .withPayload(IvrEvent.ENTER_CARD_NUMBER)
                .copyHeaders(headers)
                .build());
    }
    
    private void processPinInput(StateMachine<IvrState, IvrEvent> machine, String pin) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("pin", pin);
        machine.sendEvent(MessageBuilder
                .withPayload(IvrEvent.ENTER_PIN)
                .copyHeaders(headers)
                .build());
    }
    
    private void processErrorStateInput(StateMachine<IvrState, IvrEvent> machine, String selection) {
        if ("1".equals(selection)) {
            machine.sendEvent(IvrEvent.BACK);
        } else {
            machine.sendEvent(IvrEvent.END_CALL);
        }
    }
    
    private void processMainMenuSelection(StateMachine<IvrState, IvrEvent> machine, String selection) {
        if ("1".equals(selection)) {
            machine.sendEvent(IvrEvent.SELECT_ACCOUNT_SERVICES);
        } else if ("0".equals(selection)) {
            machine.sendEvent(IvrEvent.END_CALL);
        } else {
            logger.warn("Invalid main menu selection: {}", selection);
        }
    }
    
    private void processAccountServicesSelection(StateMachine<IvrState, IvrEvent> machine, String selection) {
        if ("1".equals(selection)) {
            machine.sendEvent(IvrEvent.SELECT_BALANCE_INQUIRY);
        } else if ("2".equals(selection)) {
            machine.sendEvent(IvrEvent.SELECT_TRANSACTION_HISTORY);
        } else if ("3".equals(selection)) {
            machine.sendEvent(IvrEvent.SELECT_TRANSFER_FUNDS);
        } else if ("9".equals(selection)) {
            machine.sendEvent(IvrEvent.BACK);
        } else {
            logger.warn("Invalid account services selection: {}", selection);
        }
    }
    
    /**
     * Create a response object based on the current state
     */
    private IvrResponse createResponseForCurrentState(String sessionId, IvrState state) {
        IvrResponse.Builder responseBuilder = IvrResponse.builder()
                .withSessionId(sessionId)
                .withCurrentState(state);
        
        switch (state) {
            case WELCOME:
                return responseBuilder
                        .withNextAction("CONNECT_CALL")
                        .withPromptMessage("Welcome to the bank IVR system")
                        .build();
                
            case AUTHENTICATION_METHOD:
                return responseBuilder
                        .withNextAction("COLLECT_AUTH_METHOD")
                        .withPromptMessage("Please select your authentication method: 1 for SSN, 2 for Debit Card")
                        .build();
                
            case SSN_PROMPT:
                return responseBuilder
                        .withNextAction("COLLECT_SSN")
                        .withPromptMessage("Please enter your Social Security Number")
                        .build();
                
            case CARD_NUMBER_PROMPT:
                return responseBuilder
                        .withNextAction("COLLECT_CARD_NUMBER")
                        .withPromptMessage("Please enter your debit card number")
                        .build();
                
            case PIN_PROMPT:
                return responseBuilder
                        .withNextAction("COLLECT_PIN")
                        .withPromptMessage("Please enter your PIN")
                        .build();
                
            case VALIDATING:
                return responseBuilder
                        .withNextAction("WAIT")
                        .withPromptMessage("Please wait while we validate your information")
                        .build();
                
            case AUTHENTICATED:
                return responseBuilder
                        .withNextAction("PROCEED_TO_MENU")
                        .withPromptMessage("You have been successfully authenticated")
                        .withAuthenticated(true)
                        .build();
                
            case ERROR:
                return responseBuilder
                        .withNextAction("COLLECT_ERROR_RESPONSE")
                        .withPromptMessage("Authentication failed. Press 1 to try again or 0 to end the call")
                        .build();
                
            case MAIN_MENU:
                return responseBuilder
                        .withNextAction("COLLECT_MENU_SELECTION")
                        .withPromptMessage("Main Menu: Press 1 for Account Services, 0 to end call")
                        .withAuthenticated(true)
                        .build();
                
            case ACCOUNT_SERVICES:
                return responseBuilder
                        .withNextAction("COLLECT_SERVICE_SELECTION")
                        .withPromptMessage("Account Services: Press 1 for Balance, 2 for Transactions, 3 for Transfers, 9 to go back")
                        .withAuthenticated(true)
                        .build();
                
            case BALANCE_INQUIRY:
                return responseBuilder
                        .withNextAction("PRESENT_BALANCE")
                        .withPromptMessage("Your current balance is $1,234.56")
                        .withAuthenticated(true)
                        .build();
                
            case TRANSACTION_HISTORY:
                return responseBuilder
                        .withNextAction("PRESENT_TRANSACTIONS")
                        .withPromptMessage("Recent transactions: $120.00 GROCERY, $45.50 GAS, $500.00 RENT")
                        .withAuthenticated(true)
                        .build();
                
            case TRANSFER_FUNDS:
                return responseBuilder
                        .withNextAction("PRESENT_TRANSFER_OPTIONS")
                        .withPromptMessage("Transfer functionality would be implemented here")
                        .withAuthenticated(true)
                        .build();
                
            case END_CALL:
                return responseBuilder
                        .withNextAction("END_CALL")
                        .withPromptMessage("Thank you for using our banking services. Goodbye!")
                        .withCallEnded(true)
                        .build();
                
            default:
                return responseBuilder
                        .withNextAction("UNKNOWN")
                        .withPromptMessage("System is in an unknown state")
                        .withErrorMessage("Unhandled state: " + state)
                        .build();
        }
    }
    
    /**
     * Add custom listeners to the state machine
     */
    private void addStateListeners(StateMachine<IvrState, IvrEvent> machine) {
        // Add state change listener to handle AUTHENTICATED state
        machine.addStateListener(new StateMachineListenerAdapter<IvrState, IvrEvent>() {
            @Override
            public void stateChanged(State<IvrState, IvrEvent> from, State<IvrState, IvrEvent> to) {
                if (from != null) {
                    logger.info("State changed from {} to {}", from.getId(), to.getId());
                    
                    // Handle VALIDATING state specially
                    if (from.getId() == IvrState.VALIDATING) {
                        logger.info("Exited VALIDATING state to {}", to.getId());
                    }
                    
                    // If we entered VALIDATING state, prepare to handle it
                    if (to.getId() == IvrState.VALIDATING) {
                        logger.info("Entered VALIDATING state, checking credentials shortly...");
                        
                        // Use a separate thread to check credentials after a short delay
                        // to allow action to complete first
                        new Thread(() -> {
                            try {
                                // Wait briefly to allow the action to process
                                Thread.sleep(100);
                                
                                // If still in VALIDATING state, force a transition
                                if (machine.getState().getId() == IvrState.VALIDATING) {
                                    logger.info("Still in VALIDATING state, checking credentials...");
                                    
                                    // Get credentials to check if authenticated
                                    CustomerCredentials credentials = machine.getExtendedState()
                                        .get("credentials", CustomerCredentials.class);
                                    
                                    if (credentials != null && credentials.isAuthenticated()) {
                                        logger.info("Credentials show authenticated=true, forcing transition to AUTHENTICATED");
                                        machine.sendEvent(IvrEvent.AUTHENTICATION_SUCCESS);
                                    } else {
                                        logger.info("Credentials show authenticated=false or not found, forcing transition to ERROR");
                                        machine.sendEvent(IvrEvent.AUTHENTICATION_FAILURE);
                                    }
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    }
                    
                    // If we entered AUTHENTICATED state, move to MAIN_MENU automatically
                    if (to.getId() == IvrState.AUTHENTICATED) {
                        logger.info("Detected transition to AUTHENTICATED state, proceeding to MAIN_MENU");
                        machine.sendEvent(IvrEvent.AUTHENTICATION_SUCCESS);
                    }
                }
            }
        });
    }
} 