package com.example.hubspot_integration.event;

import org.springframework.context.ApplicationEvent;

public class TokenReadyEvent extends ApplicationEvent {
    private final String token;

    public TokenReadyEvent(Object source, String token) {
        super(source);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}