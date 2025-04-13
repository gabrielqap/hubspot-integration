package com.example.hubspot_integration.service;

import com.example.hubspot_integration.config.OAuthProperties;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@Slf4j  
public class WebhookValidator {

    @Autowired
    private OAuthProperties oauthProps;

    public boolean isValid(String signatureHeader, String requestBody) {
        try {
            String secret = oauthProps.getClientSecret();
    
            String dataToHash = secret + requestBody;
    
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
    
            String calculatedSignature = Hex.encodeHexString(hash);
    
            if (!calculatedSignature.equals(signatureHeader)) {
                log.warn("Invalid signature received. Expected: {}, Received: {}, Request body: {}",
                calculatedSignature, signatureHeader, requestBody);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error validating signature", e);
            return false;
        }
    }
}
