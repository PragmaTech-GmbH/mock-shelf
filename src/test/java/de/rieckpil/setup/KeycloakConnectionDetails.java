package de.rieckpil.setup;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;
import org.springframework.security.oauth2.jwt.JwtDecoder;

public interface KeycloakConnectionDetails extends ConnectionDetails {
  String getAuthServerUrl();
  String getRealm();
  String getClientId();
  String getClientSecret();
  String getIssuerUri();
  String getJwkSetUri();
  JwtDecoder getJwtDecoder();
}
