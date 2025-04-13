package com.example.hubspot_integration.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.hubspot_integration.event.TokenReadyEvent;

@Component
public class TokenStore {
    private String accessToken;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void setAccessToken(String token) {
        this.accessToken = token;
        eventPublisher.publishEvent(new TokenReadyEvent(this, token));

    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public boolean hasToken() {
        return this.accessToken != null;
    }
}