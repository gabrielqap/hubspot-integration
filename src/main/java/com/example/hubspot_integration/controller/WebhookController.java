package com.example.hubspot_integration.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.hubspot_integration.model.WebhookPayload;
import com.example.hubspot_integration.model.dto.WebhookEvent;
import com.example.hubspot_integration.service.WebhookService;
import com.example.hubspot_integration.service.WebhookValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/webhook")
@Slf4j
public class WebhookController {

    @Autowired
    private WebhookValidator webhookValidator;

    @Autowired
    private WebhookService webhookService;

    @PostMapping("/contact-created")
    public ResponseEntity<String> handleContactCreationWebhook(
            @RequestHeader("X-HubSpot-Signature") String signature,
            @RequestBody String requestBody,
            ObjectMapper mapper
    ) {
        if (!webhookValidator.isValid(signature, requestBody)) {
            log.warn("Invalid webhook signature received: " + signature);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        try {
            ObjectMapper newMapper = new ObjectMapper();

            List<WebhookEvent> events = newMapper.readValue(requestBody, new TypeReference<List<WebhookEvent>>(){});

            webhookService.processWebhook(events);
            log.info("Webhook event processed successfully. Payload: {}", requestBody);
            return ResponseEntity.ok("Event processed successfully.");
        } catch (Exception e) {
            log.error("Failed to process webhook event. Payload: {}", requestBody, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to process event: " + e.getMessage());
        }
    }
}