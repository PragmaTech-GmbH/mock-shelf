package de.rieckpil.setup;

import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public class KeycloakContainerConnectionDetailsFactory
  extends ContainerConnectionDetailsFactory<KeycloakContainer, KeycloakConnectionDetails> {

  @Override
  protected KeycloakConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<KeycloakContainer> source) {
    KeycloakContainer container = null;
    String baseUrl = String.format("http://%s", container.getIssuerUrl());

    return new KeycloakConnectionDetails() {
      @Override
      public String getIssuerUri() {
        return baseUrl + "/realms/mockshelf";
      }

      @Override
      public JwtDecoder getJwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(getIssuerUri() + "/protocol/openid-connect/certs").build();
      }
    };
  }
}
