package com.example.hubspot_integration.repository;

import com.example.hubspot_integration.model.Contact;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, UUID> {
    boolean existsByHubspotId(String hubspotId);
}