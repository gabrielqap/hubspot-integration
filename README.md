# HubSpot Integration API

A Java Spring Boot REST API for integrating with HubSpot CRM using OAuth 2.0 (Authorization Code Flow).  
The project includes endpoints for contact creation, webhook handling (with HMAC validation), and asynchronous processing using RabbitMQ.

## Technologies Used

- Java 17
- Spring Boot
- Spring Web
- Spring Security (OAuth2 Client)
- Spring Data JPA
- H2 Database (in-memory)
- RabbitMQ (via Spring AMQP)
- Docker & Docker Compose

## Features

- OAuth 2.0 Authorization Code Flow integration with HubSpot
- Contact creation via HubSpot API
- Webhook listener for `contact.creation` events
- HMAC SHA256 validation of webhook signatures
- Asynchronous message processing with RabbitMQ
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

3. Start the application and RabbitMQ with Docker Compose

   docker-compose up --build

   - The application will be available at: http://localhost:8080  
   - RabbitMQ management panel: http://localhost:15672 (user: guest, password: guest)  
   - H2 console: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:testdb)

## API Endpoints

### Authorization

- GET /oauth/authorize  
  Returns the HubSpot authorization URL

- GET /oauth/callback?code=...  
  Exchanges the authorization code for an access token

### Contacts

- POST /contacts  
  Creates a contact on HubSpot using the access token

### Webhook

- POST /webhook/contact-created  
  Endpoint that receives HubSpot `contact.creation` webhook events  
  Validates signature using HMAC SHA256  
  Publishes events to RabbitMQ

## Queue

- Queue name: webhook.events  
- RabbitMQ is used to asynchronously process contact creation events

## Notes

- This project uses an in-memory H2 database for simplicity  
- Tokens are stored in memory (you can extend it to persist in the database)  
- You can test the webhook by exposing your local server using `ngrok`