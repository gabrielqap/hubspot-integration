package com.example.hubspot_integration.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.hubspot_integration.model.Contact;
import com.example.hubspot_integration.model.ContactMapper;
import com.example.hubspot_integration.model.dto.ContactDTO;
import com.example.hubspot_integration.repository.ContactRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private HubSpotService hubSpotService;

    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    public Contact createContact(ContactDTO contactDTO) {
        String responseBody = hubSpotService.createContactInHubSpot(contactDTO);
        Contact contact = saveContactInRepository(responseBody, contactDTO);
        return contact;
    }

    private Contact saveContactInRepository(String responseBody, ContactDTO dto) {
        try {
            String hubspotId = extractHubSpotId(responseBody);
            Contact contact = ContactMapper.fromDTO(dto, hubspotId);
            contactRepository.save(contact);

            contactRepository.save(contact);
            return contact;

        } catch (Exception e) {
            String errorMessage = String.format(
                "Failed to save contact (Email: %s, Firstname: %s, Lastname: %s): %s", 
                dto.email(), dto.firstname(), dto.lastname(), e.getMessage()
            );
            log.error(errorMessage, e);
            throw new RuntimeException("Failed to parse HubSpot response: " + e.getMessage(), e);
        }
    }

    private String extractHubSpotId(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            return root.get("id").asText();
        } catch (Exception e) {
            log.error("Failed to extract HubSpot ID, Body: {}", responseBody);
            throw new RuntimeException("Failed to extract HubSpot ID: " + e.getMessage(), e);
        }
    }
}