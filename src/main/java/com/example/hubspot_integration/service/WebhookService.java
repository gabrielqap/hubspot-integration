package com.example.hubspot_integration.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.hubspot_integration.model.Contact;
import com.example.hubspot_integration.model.ContactMapper;
import com.example.hubspot_integration.model.WebhookPayload;
import com.example.hubspot_integration.model.dto.ContactDTO;
import com.example.hubspot_integration.model.dto.WebhookEvent;
import com.example.hubspot_integration.repository.ContactRepository;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class WebhookService {

    @Autowired
    private HubSpotService hubSpotService;

    @Autowired
    private ContactRepository contactRepository;

    public void processWebhook(List<WebhookEvent> events) {
        for (WebhookEvent event : events) {
            if ("contact.creation".equalsIgnoreCase(event.subscriptionType())) {
                String objectId = event.objectId();
                
                if (contactRepository.existsByHubspotId(objectId)) {
                    log.info("Contact already exists in database with hubspotId {}. Skipping creation.", objectId);
                    continue;
                }

                try {
                    ContactDTO contactDTO = hubSpotService.getContactById(objectId);
                    Contact contact = ContactMapper.fromDTO(contactDTO, null);
                    contactRepository.save(contact);
                    log.info("New contact created with hubspotId {} and saved to database.", objectId);
                } catch (Exception e) {
                    log.error("Failed to fetch contact details for hubspotId {} from HubSpot API: {}", objectId, e.getMessage());
                }
            }
        }
    }
}