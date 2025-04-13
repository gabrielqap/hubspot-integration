package com.example.hubspot_integration.service;

import com.example.hubspot_integration.config.TokenStore;
import com.example.hubspot_integration.model.ContactMapper;
import com.example.hubspot_integration.model.dto.ContactDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HubSpotService {

    @Value("${hubspot.contacts-uri}")
    private String contactsApiURL;

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_DELAY_MS = 1000L;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private ObjectMapper objectMapper;

    public String createContactInHubSpot(ContactDTO contactDTO) {
        validateContactDTO(contactDTO);
        validateToken();
    
        Map<String, Object> requestBody = buildRequestBody(contactDTO);
        ResponseEntity<String> response = sendCreateContactRequest(requestBody);
    
        if (!response.getStatusCode().is2xxSuccessful()) {
            String requestId = response.getHeaders().getFirst("X-Request-Id");
            log.error("Failed to create contact in HubSpot. Status code: {}, Response body: {}, X-Request-Id: {}",
                      response.getStatusCode(), response.getBody(), requestId);
            throw new RuntimeException("Failed to create contact in HubSpot: " + response.getBody());
        }
    
        return response.getBody();
    }

    private void validateContactDTO(ContactDTO dto) {
        if (dto.email() == null || dto.firstname() == null || dto.lastname() == null) {
            throw new IllegalArgumentException("Email, firstname, and lastname are required and cannot be null");
        }
    }

    private Map<String, Object> buildRequestBody(ContactDTO dto) {
        Map<String, Object> body = new HashMap<>();
        body.put("properties", ContactMapper.toProperties(dto));
    
        return body;
    }

    private ResponseEntity<String> sendCreateContactRequest(Map<String, Object> requestBody) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenStore.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
    
        int attempt = 0;
        long delay = INITIAL_DELAY_MS;
    
        while (attempt < MAX_RETRIES) {
            ResponseEntity<String> response = restTemplate.postForEntity(
                contactsApiURL, request, String.class
            );
    
            if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                attempt++;
                String requestId = response.getHeaders().getFirst("X-Request-Id");
                log.warn("Rate limit reached (429). Retrying attempt {}/{} after {} ms... X-Request-Id: {}", 
                         attempt, MAX_RETRIES, delay, requestId);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted during retry delay", ie);
                }
                delay *= 2;
            } else {
                return response;
            }
        }

        log.error("Max retries exceeded. HubSpot is still responding with HTTP 429 (rate limit) after {} attempts.", MAX_RETRIES);
        throw new RuntimeException("Max retries exceeded due to HubSpot rate limiting (HTTP 429).");
    }

    public ContactDTO getContactById(String contactId) {
        validateToken();
        
        String url = contactsApiURL + "/" + contactId;
        ResponseEntity<String> response = sendGetRequest(url);
    
        if (!response.getStatusCode().is2xxSuccessful()) {
            String requestId = response.getHeaders().getFirst("X-Request-Id");
            log.error("Failed to fetch contact from HubSpot. Status: {}, Body: {}, X-Request-Id: {}",
                  response.getStatusCode(), response.getBody(), requestId);
            throw new RuntimeException("Failed to fetch contact from HubSpot: " + response.getBody());
        }
    
        return parseContactResponse(response.getBody());
    }

    private void validateToken() {
        if (!tokenStore.hasToken()) {
            throw new RuntimeException("Access token is not available");
        }
    }

    private ResponseEntity<String> sendGetRequest(String url) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenStore.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    private ContactDTO parseContactResponse(String responseBody) {
        try {
            JsonNode json = objectMapper.readTree(responseBody);
            return ContactMapper.fromJson(json);
        } catch (Exception e) {
            log.error("Failed to parse HubSpot response. Body: {}", responseBody, e);
            throw new RuntimeException("Failed to parse HubSpot response: " + e.getMessage(), e);
        }
    }
    
    public long getTotalContacts() {
        String url = contactsApiURL + "?limit=100";
        long totalContacts = 0;
        String after = null;
    
        do {
            if (after != null) {
                url = contactsApiURL + "?limit=100&after=" + after;
            }
    
            ResponseEntity<String> response = sendGetRequest(url);
    
            JsonNode body = null;
            try {
                body = objectMapper.readTree(response.getBody());
                log.info("Response body: {}", body.toString());
            } catch (JsonMappingException e) {
                log.error("Error mapping JSON response: {}", e.getMessage(), e);
            } catch (JsonProcessingException e) {
                log.error("Error processing JSON response: {}", e.getMessage(), e);
            }
    
            if (body != null) {
                totalContacts += body.path("results").size();
            }
    
            JsonNode paging = body.path("paging");
            if (paging.has("next")) {
                after = paging.path("next").path("after").asText();
                log.info("More contacts to fetch. Next 'after' value: {}", after);
            } else {
                after = null;
            }
        } while (after != null);
    
        log.info("Total contacts fetched: {}", totalContacts);
        return totalContacts;
    }

    public List<ContactDTO> fetchContacts(int offset, int limit) {
        String url = contactsApiURL + "?limit=" + limit + "&after=" + offset;
        log.info("Fetching contacts from HubSpot API: {}", url);

        ResponseEntity<String> response = sendGetRequest(url);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                
                objectMapper.registerModule(new JavaTimeModule());
                
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode results = root.get("results");

                List<ContactDTO> contacts = new ArrayList<>();
                if (results != null && results.isArray()) {
                    for (JsonNode node : results) {
                        ContactDTO contact = ContactMapper.fromJson(node);
                        contacts.add(contact);
                    }
                    log.info("Successfully parsed {} contacts from API response.", contacts.size());
                } else {
                    log.warn("No contacts found in API response.");
                }

                return contacts;
            } catch (Exception e) {
                log.error("Error while parsing contacts from response: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to parse contacts from API response.", e);
            }
        } else {
            log.error("Failed to fetch contacts from HubSpot. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            throw new RuntimeException("Failed to fetch contacts from HubSpot. Status: "
                    + response.getStatusCode() + " - " + response.getBody());
        }
    }
}