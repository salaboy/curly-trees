package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.EnumSet;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Autowired
    private StateMachine<States, Events> stateMachine;

    @Override
    public void run(String... args) throws Exception {
        Message<Events> messageEvent1 = MessageBuilder.withPayload(Events.E1).build();
        Message<Events> messageEvent2 = MessageBuilder.withPayload(Events.E2).build();
        stateMachine.sendEvent(Mono.just(messageEvent1)).doOnComplete(() -> {
            System.out.println("Event 1 handling complete");
        }).subscribe();
        stateMachine.sendEvent(Mono.just(messageEvent2)).doOnComplete(() -> {
            System.out.println("Event 1 handling complete");
        }).subscribe();

    }

}


enum States {
    SI, S1, S2
}

enum Events {
    E1, E2
}

@Configuration
@EnableStateMachine
class StateMachineConfig
        extends EnumStateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config)
            throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states)
            throws Exception {
        states
                .withStates()
                .initial(States.SI)
                .states(EnumSet.allOf(States.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
            throws Exception {
        transitions
                .withExternal()
                .source(States.SI).target(States.S1).event(Events.E1).action(action())
                .and()
                .withExternal()
                .source(States.S1).target(States.S2).event(Events.E2).action(action());
    }

    @Bean
    public StateMachineListener<States, Events> listener() {
        return new StateMachineListenerAdapter<States, Events>() {
            @Override
            public void stateChanged(State<States, Events> from, State<States, Events> to) {
                System.out.println("State change to " + to.getId());
            }
        };
    }

    @Bean
    public MyAction action() {
        return new MyAction();
    }

    static class MyAction implements Action<States, Events> {

        @Override
        public void execute(StateContext<States, Events> stateContext) {
            System.out.println("> Action Event: " + stateContext.getEvent().name());
            System.out.println("> Action Source: " + stateContext.getSource().getId());
            System.out.println("> Action Target: " + stateContext.getTarget().getId());
        }

    }





}


