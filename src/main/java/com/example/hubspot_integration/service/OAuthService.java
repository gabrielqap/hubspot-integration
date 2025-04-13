package com.example.hubspot_integration.service;

import com.example.hubspot_integration.config.OAuthProperties;
import com.example.hubspot_integration.config.TokenStore;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class OAuthService {

    @Autowired
    private OAuthProperties oauthProps;

    @Autowired
    private TokenStore tokenStore;

    public void exchangeCodeForToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", oauthProps.getGrantType());
        params.add("client_id", oauthProps.getClientId());
        params.add("client_secret", oauthProps.getClientSecret());
        params.add("redirect_uri", oauthProps.getRedirectUri());
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            oauthProps.getTokenUri(),
            HttpMethod.POST,
            request,
            new ParameterizedTypeReference<>() {}
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String accessToken = (String) response.getBody().get("access_token");
            tokenStore.setAccessToken(accessToken);
        } else {
            log.error("Failed to exchange code for token: {} - {}", response.getStatusCode(), response.getBody());
            throw new RuntimeException("Failed to exchange code for token: " +
                response.getStatusCode() + " - " + response.getBody());
        }
    }
}