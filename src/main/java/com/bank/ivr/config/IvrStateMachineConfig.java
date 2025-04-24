package com.bank.ivr.config;

import com.bank.ivr.action.AuthenticationActions;
import com.bank.ivr.model.IvrEvent;
import com.bank.ivr.model.IvrState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class IvrStateMachineConfig extends EnumStateMachineConfigurerAdapter<IvrState, IvrEvent> {

    private static final Logger logger = LoggerFactory.getLogger(IvrStateMachineConfig.class);

    @Autowired
    private AuthenticationActions authActions;

    @Override
    public void configure(StateMachineStateConfigurer<IvrState, IvrEvent> states) throws Exception {
        states
            .withStates()
                .initial(IvrState.WELCOME)
                .states(EnumSet.allOf(IvrState.class))
                .end(IvrState.END_CALL);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<IvrState, IvrEvent> transitions) throws Exception {
        transitions
            // Welcome state transitions
            .withExternal()
                .source(IvrState.WELCOME)
                .target(IvrState.AUTHENTICATION_METHOD)
                .event(IvrEvent.CALL_CONNECTED)
                .and()
            
            // Authentication method selection transitions
            .withExternal()
                .source(IvrState.AUTHENTICATION_METHOD)
                .target(IvrState.SSN_PROMPT)
                .event(IvrEvent.SELECT_SSN_AUTH)
                .and()
            .withExternal()
                .source(IvrState.AUTHENTICATION_METHOD)
                .target(IvrState.CARD_NUMBER_PROMPT)
                .event(IvrEvent.SELECT_CARD_AUTH)
                .and()
                
            // SSN authentication path
            .withExternal()
                .source(IvrState.SSN_PROMPT)
                .target(IvrState.VALIDATING)
                .event(IvrEvent.ENTER_SSN)
                .action(authActions.validateSsn())
                .and()
                
            // Card authentication path
            .withExternal()
                .source(IvrState.CARD_NUMBER_PROMPT)
                .target(IvrState.PIN_PROMPT)
                .event(IvrEvent.ENTER_CARD_NUMBER)
                .action(authActions.validateCardNumber())
                .and()
            .withExternal()
                .source(IvrState.PIN_PROMPT)
                .target(IvrState.VALIDATING)
                .event(IvrEvent.ENTER_PIN)
                .action(authActions.validatePin())
                .and()
                
            // Validation outcomes
            .withExternal()
                .source(IvrState.VALIDATING)
                .target(IvrState.AUTHENTICATED)
                .event(IvrEvent.AUTHENTICATION_SUCCESS)
                .and()
            .withExternal()
                .source(IvrState.VALIDATING)
                .target(IvrState.ERROR)
                .event(IvrEvent.AUTHENTICATION_FAILURE)
                .and()
            .withExternal()
                .source(IvrState.ERROR)
                .target(IvrState.AUTHENTICATION_METHOD)
                .event(IvrEvent.BACK)
                .and()
                
            // Post-authentication menu options
            .withExternal()
                .source(IvrState.AUTHENTICATED)
                .target(IvrState.MAIN_MENU)
                .event(IvrEvent.AUTHENTICATION_SUCCESS)
                .and()
            .withExternal()
                .source(IvrState.MAIN_MENU)
                .target(IvrState.ACCOUNT_SERVICES)
                .event(IvrEvent.SELECT_ACCOUNT_SERVICES)
                .and()
            .withExternal()
                .source(IvrState.ACCOUNT_SERVICES)
                .target(IvrState.BALANCE_INQUIRY)
                .event(IvrEvent.SELECT_BALANCE_INQUIRY)
                .and()
            .withExternal()
                .source(IvrState.ACCOUNT_SERVICES)
                .target(IvrState.TRANSACTION_HISTORY)
                .event(IvrEvent.SELECT_TRANSACTION_HISTORY)
                .and()
            .withExternal()
                .source(IvrState.ACCOUNT_SERVICES)
                .target(IvrState.TRANSFER_FUNDS)
                .event(IvrEvent.SELECT_TRANSFER_FUNDS)
                .and()
                
            // Return to main menu
            .withExternal()
                .source(IvrState.ACCOUNT_SERVICES)
                .target(IvrState.MAIN_MENU)
                .event(IvrEvent.BACK)
                .and()
                
            // Transaction completion
            .withExternal()
                .source(IvrState.BALANCE_INQUIRY)
                .target(IvrState.MAIN_MENU)
                .event(IvrEvent.COMPLETE_TRANSACTION)
                .and()
            .withExternal()
                .source(IvrState.TRANSACTION_HISTORY)
                .target(IvrState.MAIN_MENU)
                .event(IvrEvent.COMPLETE_TRANSACTION)
                .and()
            .withExternal()
                .source(IvrState.TRANSFER_FUNDS)
                .target(IvrState.MAIN_MENU)
                .event(IvrEvent.COMPLETE_TRANSACTION)
                .and()
                
            // End call from anywhere
            .withExternal()
                .source(IvrState.MAIN_MENU)
                .target(IvrState.END_CALL)
                .event(IvrEvent.END_CALL)
                .and()
            .withExternal()
                .source(IvrState.ERROR)
                .target(IvrState.END_CALL)
                .event(IvrEvent.END_CALL);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<IvrState, IvrEvent> config) throws Exception {
        config
            .withConfiguration()
                .autoStartup(true)
                .listener(listener());
    }

    @Bean
    public StateMachineListener<IvrState, IvrEvent> listener() {
        return new StateMachineListenerAdapter<IvrState, IvrEvent>() {
            @Override
            public void stateChanged(State<IvrState, IvrEvent> from, State<IvrState, IvrEvent> to) {
                if (from != null) {
                    logger.info("State change from {} to {}", from.getId(), to.getId());
                }
            }
        };
    }
} 