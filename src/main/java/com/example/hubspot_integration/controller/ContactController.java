package com.example.hubspot_integration.controller;


import com.example.hubspot_integration.model.Contact;
import com.example.hubspot_integration.model.dto.ContactDTO;
import com.example.hubspot_integration.service.ContactService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contacts")
@Slf4j
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping
    public ResponseEntity<?> createContact(@RequestBody ContactDTO contactDTO) {
        try {
            Contact contact = contactService.createContact(contactDTO);
            return new ResponseEntity<>(contact, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Failed to create contact: " + contactDTO.toString(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to create contact: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        List<Contact> contacts = contactService.getAllContacts();
        return ResponseEntity.ok(contacts);
    }

    
}