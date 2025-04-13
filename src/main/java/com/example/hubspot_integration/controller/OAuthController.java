package com.example.hubspot_integration.controller;

import com.example.hubspot_integration.config.OAuthProperties;
import com.example.hubspot_integration.service.OAuthService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/oauth")
@Slf4j
public class OAuthController {

    @Autowired
    private OAuthService oauthService;

    @Autowired
    private OAuthProperties oauthProps;

    @GetMapping("/authorize")
    public ResponseEntity<String> getAuthorizationUrl() {
        String url = UriComponentsBuilder
                .fromUriString(oauthProps.getAuthorizationUri())
                .queryParam("client_id", oauthProps.getClientId())
                .queryParam("redirect_uri", oauthProps.getRedirectUri())
                .queryParam("scope", oauthProps.getScope())
                .queryParam("response_type", oauthProps.getResponseType())
                .build()
                .toUriString();

        return ResponseEntity.ok(url);
    }

    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestParam("code") String code) {
        try {
            log.info("Received authorization code: {}", code);
            oauthService.exchangeCodeForToken(code);     
            log.info("Token successfully saved for code: {}.", code);
            return ResponseEntity.ok("Token saved successfully!");
        } catch (Exception e) {
            log.error("Error processing callback: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error processing callback: " + e.getMessage());
        }
    }
}