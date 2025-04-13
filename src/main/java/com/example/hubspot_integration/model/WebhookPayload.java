package com.example.hubspot_integration.model;
import java.util.List;

import com.example.hubspot_integration.model.dto.WebhookEvent;

import lombok.Data;

@Data
public class WebhookPayload {
    private List<WebhookEvent> events;
}