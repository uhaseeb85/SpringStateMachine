package com.bank.ivr.action;

import com.bank.ivr.model.CustomerCredentials;
import com.bank.ivr.model.IvrEvent;
import com.bank.ivr.model.IvrState;
import com.bank.ivr.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

/**
 * Action handlers for the authentication process.
 */
@Component
public class AuthenticationActions {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationActions.class);

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationActions(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Action to handle SSN entry and validation.
     */
    public Action<IvrState, IvrEvent> validateSsn() {
        return context -> {
            try {
                CustomerCredentials credentials = getOrCreateCredentials(context);
                String ssn = (String) context.getMessageHeader("ssn");
                
                logger.debug("Validating SSN: {}", maskSsn(ssn));
                boolean isValid = authenticationService.authenticateBySSN(ssn);
                credentials.setSsn(ssn);
                
                if (isValid) {
                    logger.info("Authentication successful with SSN");
                    credentials.setAuthenticated(true);
                    credentials.setCustomerId(authenticationService.getAuthenticatedCustomerId());
                    boolean accepted = context.getStateMachine().sendEvent(IvrEvent.AUTHENTICATION_SUCCESS);
                    logger.debug("AUTHENTICATION_SUCCESS event accepted: {}", accepted);
                } else {
                    logger.info("Authentication failed with SSN");
                    credentials.setAuthenticated(false);
                    boolean accepted = context.getStateMachine().sendEvent(IvrEvent.AUTHENTICATION_FAILURE);
                    logger.debug("AUTHENTICATION_FAILURE event accepted: {}", accepted);
                }
            } catch (Exception e) {
                logger.error("Exception during SSN validation", e);
                // Make sure we still send the failure event in case of exceptions
                boolean accepted = context.getStateMachine().sendEvent(IvrEvent.AUTHENTICATION_FAILURE);
                logger.debug("AUTHENTICATION_FAILURE event accepted after exception: {}", accepted);
            }
        };
    }

    /**
     * Action to handle card entry.
     */
    public Action<IvrState, IvrEvent> validateCardNumber() {
        return context -> {
            CustomerCredentials credentials = getOrCreateCredentials(context);
            String cardNumber = (String) context.getMessageHeader("cardNumber");
            logger.debug("Storing card number: {}", maskCardNumber(cardNumber));
            credentials.setCardNumber(cardNumber);
            
            // Just store the card number and proceed to PIN entry
            // Validation will happen after PIN entry
        };
    }

    /**
     * Action to handle PIN validation.
     */
    public Action<IvrState, IvrEvent> validatePin() {
        return context -> {
            try {
                CustomerCredentials credentials = getOrCreateCredentials(context);
                String pin = (String) context.getMessageHeader("pin");
                
                logger.debug("Validating card and PIN");
                boolean isValid = authenticationService.authenticateByCardAndPin(
                    credentials.getCardNumber(), pin);
                credentials.setPin(pin);
                
                if (isValid) {
                    logger.info("Authentication successful with card/PIN");
                    credentials.setAuthenticated(true);
                    credentials.setCustomerId(authenticationService.getAuthenticatedCustomerId());
                    boolean accepted = context.getStateMachine().sendEvent(IvrEvent.AUTHENTICATION_SUCCESS);
                    logger.debug("AUTHENTICATION_SUCCESS event accepted: {}", accepted);
                } else {
                    logger.info("Authentication failed with card/PIN");
                    credentials.setAuthenticated(false);
                    boolean accepted = context.getStateMachine().sendEvent(IvrEvent.AUTHENTICATION_FAILURE);
                    logger.debug("AUTHENTICATION_FAILURE event accepted: {}", accepted);
                }
            } catch (Exception e) {
                logger.error("Exception during PIN validation", e);
                // Make sure we still send the failure event in case of exceptions
                boolean accepted = context.getStateMachine().sendEvent(IvrEvent.AUTHENTICATION_FAILURE);
                logger.debug("AUTHENTICATION_FAILURE event accepted after exception: {}", accepted);
            }
        };
    }

    /**
     * Helper method to get or create the credentials object in the state machine context.
     */
    private CustomerCredentials getOrCreateCredentials(StateContext<IvrState, IvrEvent> context) {
        CustomerCredentials credentials = context.getExtendedState().get("credentials", CustomerCredentials.class);
        if (credentials == null) {
            credentials = new CustomerCredentials();
            context.getExtendedState().getVariables().put("credentials", credentials);
        }
        return credentials;
    }
    
    /**
     * Helper method to mask SSN for logging purposes
     */
    private String maskSsn(String ssn) {
        if (ssn == null || ssn.length() < 5) {
            return "INVALID_SSN";
        }
        return "XXX-XX-" + ssn.substring(ssn.length() - 4);
    }
    
    /**
     * Helper method to mask card number for logging purposes
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "INVALID_CARD";
        }
        return "XXXX-XXXX-XXXX-" + cardNumber.substring(cardNumber.length() - 4);
    }
} 