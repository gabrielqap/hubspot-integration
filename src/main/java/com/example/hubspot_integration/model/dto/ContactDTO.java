package com.example.hubspot_integration.model.dto;

public record ContactDTO(
    String id,
    String email,
    String firstname,
    String lastname,
    String createdAt, 
    String updatedAt,
    String phone,
    String jobTitle,
    String company,
    String leadStatus,
    String lifecycleStage,
    String website,
    String address
) {}