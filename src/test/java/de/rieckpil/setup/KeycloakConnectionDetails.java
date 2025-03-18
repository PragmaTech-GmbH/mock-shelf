package de.rieckpil.setup;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;
import org.springframework.security.oauth2.jwt.JwtDecoder;

public interface KeycloakConnectionDetails extends ConnectionDetails {
  /**
   * Gets the issuer URI for Keycloak.
   *
   * @return the issuer URI
   */
  String getIssuerUri();

  /**
   * Gets a configured JWT decoder for validation.
   *
   * @return the JWT decoder
   */
  JwtDecoder getJwtDecoder();
}
