# HubSpot Integration API

A Java Spring Boot REST API to integrate with HubSpot CRM using OAuth 2.0 Authorization Code Flow.  
Supports contact creation, full contact sync, and webhook handling with HMAC validation.

## Technologies Used

- Java 17
- Spring Boot
- Spring Web
- Spring Security (OAuth2 Client)
- Spring Data JPA
- H2 Database (in-memory)
- Docker & Docker Compose

## Features

**HubSpot Integration**
- OAuth 2.0 Authorization Code Flow integration
- Automatically fetches and stores all contacts after successful OAuth
- Contact creation via HubSpot API
- Full contact synchronization from HubSpot to local database

**Webhook Handling**
- Webhook listener for `contact.creation` events
- Automatically fetches full contact data upon webhook reception
- HMAC SHA256 validation for webhook security

**Tech Stack**
- Spring Boot backend with H2 in-memory database
- Full containerized setup with Docker

## How to Run (Docker)

1. Clone the repository

   git clone https://github.com/your-username/hubspot-integration.git
   cd hubspot-integration

2. Configure your credentials

   Edit `application.properties` or use environment variables:

   hubspot.client-id=YOUR_CLIENT_ID  
   hubspot.client-secret=YOUR_CLIENT_SECRET  
   hubspot.redirect-uri=http://localhost:8080/oauth/callback

3. Start the application using Docker Compose:

   docker-compose up --build

   The API will be accessible at: http://localhost:8080

## API Endpoints

### Authorization

- `GET /oauth/authorize`  
  Returns the HubSpot authorization URL.

- `GET /oauth/callback?code=...`  
  Exchanges the authorization code for an access token.  
  Then verifies and syncs all HubSpot contacts to the local database.

The routes below are only available after authorizing the integration and obtaining the access token.

### Contacts

- `POST /contacts`  
  Creates a contact on HubSpot using the access token.

- `GET /contacts`  
  Returns all contacts stored in the local database.

### Webhook

- `POST /webhook/contact-created`  
  Handles `contact.creation` webhook events from HubSpot.  
  Validates the request signature using HMAC SHA256.  
  Fetches the full contact from the API and stores it locally.
  
## Notes

- This project uses an in-memory H2 database for simplicity.
- OAuth access tokens are stored in memory (not persisted).
- You can test the webhook locally by exposing your server using `ngrok`.
- Remember to configure the webhook in your HubSpot app to send `contact.creation` events to the `ngrok` URL (e.g. `https://abc123.ngrok.io/webhook/contact-created`).
