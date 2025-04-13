package com.example.hubspot_integration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.UUID;


@Entity
@Data
public class Contact {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false)
    private String hubspotId;
    private String email;
    private String firstname;
    private String lastname;
    private String createdAt;
    private String updatedAt;
    private String phone;
    private String jobTitle;
    private String company;
    private String leadStatus;
    private String lifecycleStage;
    private String website;
    private String address;
}