package com.example.hubspot_integration.model;

import java.util.HashMap;
import java.util.Map;

import com.example.hubspot_integration.model.dto.ContactDTO;
import com.fasterxml.jackson.databind.JsonNode;

public class ContactMapper {

    private ContactMapper() {}

    public static Contact fromDTO(ContactDTO dto, String hubspotId) {
        Contact contact = new Contact();
        contact.setHubspotId(hubspotId != null ? hubspotId : dto.id());
        contact.setEmail(dto.email());
        contact.setFirstname(dto.firstname());
        contact.setLastname(dto.lastname());
        contact.setPhone(dto.phone());
        contact.setJobTitle(dto.jobTitle());
        contact.setCompany(dto.company());
        contact.setLeadStatus(dto.leadStatus());
        contact.setLifecycleStage(dto.lifecycleStage());
        contact.setWebsite(dto.website());
        contact.setAddress(dto.address());
        contact.setCreatedAt(dto.createdAt());
        contact.setUpdatedAt(dto.updatedAt());
        return contact;
    }

    public static Map<String, Object> toProperties(ContactDTO dto) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("email", dto.email());
        properties.put("firstname", dto.firstname());
        properties.put("lastname", dto.lastname());
        putIfNotNull(properties, "phone", dto.phone());
        putIfNotNull(properties, "jobtitle", dto.jobTitle());
        putIfNotNull(properties, "company", dto.company());
        putIfNotNull(properties, "hs_lead_status", dto.leadStatus());
        putIfNotNull(properties, "lifecyclestage", dto.lifecycleStage());
        putIfNotNull(properties, "website", dto.website());
        putIfNotNull(properties, "address", dto.address());
        return properties;
    }

    public static ContactDTO fromJson(JsonNode json) {
        JsonNode properties = json.get("properties");

        return new ContactDTO(
            json.path("id").asText(),
            getSafeText(properties, "email"),
            getSafeText(properties, "firstname"),
            getSafeText(properties, "lastname"),
            json.path("createdAt").asText(),
            json.path("updatedAt").asText(),
            getSafeText(properties, "phone"),
            getSafeText(properties, "jobtitle"),
            getSafeText(properties, "company"),
            getSafeText(properties, "hs_lead_status"),
            getSafeText(properties, "lifecyclestage"),
            getSafeText(properties, "website"),
            getSafeText(properties, "address")
        );
    }

    private static String getSafeText(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null && !field.isNull() ? field.asText() : null;
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) map.put(key, value);
    }
}