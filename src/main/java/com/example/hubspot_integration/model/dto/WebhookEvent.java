package com.example.hubspot_integration.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WebhookEvent(
    @JsonProperty("subscriptionId") Long subscriptionId,
    String subscriptionType,
    String objectId,
    Long occurredAt,
    @JsonProperty("portalId") Long portalId,
    @JsonProperty("appId") Long appId,
    @JsonProperty("attemptNumber") Integer attemptNumber,
    @JsonProperty("changeFlag") String changeFlag,
    @JsonProperty("changeSource") String changeSource,
    @JsonProperty("sourceId") String sourceId,
    String eventId
) {}