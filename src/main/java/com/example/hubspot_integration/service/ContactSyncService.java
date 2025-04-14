package com.example.hubspot_integration.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.example.hubspot_integration.event.TokenReadyEvent;
import com.example.hubspot_integration.model.Contact;
import com.example.hubspot_integration.model.ContactMapper;
import com.example.hubspot_integration.model.dto.ContactDTO;
import com.example.hubspot_integration.repository.ContactRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ContactSyncService {
    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private HubSpotService hubSpotService;

    @EventListener
    public void onTokenReady(TokenReadyEvent event) {
        log.info("Token received! Starting synchronization with HubSpot.");
        syncContacts();
    }


    public void syncContacts() {
        long localCount = contactRepository.count();
        long hubspotCount = hubSpotService.getTotalContacts();

        if (localCount < hubspotCount) {
            log.info("Synchronizing missing contacts from HubSpot: local={}, remote={}", localCount, hubspotCount);

            int pageSize = 100;
            int offset = (int) localCount;

            while (offset < hubspotCount) {
                List<ContactDTO> contacts = hubSpotService.fetchContacts(offset, pageSize);
                contactRepository.saveAll(convertToEntities(contacts));
                offset += pageSize;
            }

            log.info("Synchronization completed.");
        } else {
            log.info("No synchronization needed. Contacts are already up-to-date.");
        }
    }

    private List<Contact> convertToEntities(List<ContactDTO> dtos) {
        return dtos.stream().map(dto -> {
        Contact contact = ContactMapper.fromDTO(dto, null);
        contactRepository.save(contact);
            return contact;
        }).toList();
    }

}
