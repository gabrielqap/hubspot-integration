package com.example.hubspot_integration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "hubspot")
@Data
public class OAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String tokenUri;
    private String scope;
    private String responseType;
    private String grantType;
    private String authorizationUri;
}