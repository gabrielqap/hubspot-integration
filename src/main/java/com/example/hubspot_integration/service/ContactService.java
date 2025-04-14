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

    @Autowired
    private ObjectMapper objectMapper;

    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    public Contact createContact(ContactDTO contactDTO) {
        String responseBody = hubSpotService.createContactInHubSpot(contactDTO);
        return saveContactInRepository(responseBody, contactDTO);
    }
    
    private Contact saveContactInRepository(String responseBody, ContactDTO dto) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
    
            String hubspotId = root.get("id").asText();
            Contact contact = ContactMapper.fromDTO(dto, hubspotId);
    
            JsonNode properties = root.get("properties");
            if (properties != null) {
                contact.setCreatedAt(properties.get("createdate").asText());
                contact.setUpdatedAt(properties.get("lastmodifieddate").asText());
            }
    
            return contactRepository.save(contact);
    
        } catch (Exception e) {
            String errorMessage = String.format(
                "Failed to save contact (Email: %s, Firstname: %s, Lastname: %s): %s", 
                dto.email(), dto.firstname(), dto.lastname(), e.getMessage()
            );
            log.error(errorMessage, e);
            throw new RuntimeException("Failed to parse HubSpot response: " + e.getMessage(), e);
        }
    }
}