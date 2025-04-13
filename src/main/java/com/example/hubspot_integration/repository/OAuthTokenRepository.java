package com.example.hubspot_integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hubspot_integration.model.OAuthToken;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
}