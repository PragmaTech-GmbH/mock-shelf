package de.rieckpil.setup;

import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.springframework.security.oauth2.jwt.JwtDecoder;

public class KeycloakContainerConnectionDetailsFactory
  extends ContainerConnectionDetailsFactory<KeycloakContainer, KeycloakConnectionDetails> {

  @Override
  protected KeycloakConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<KeycloakContainer> source) {
    return new KeycloakContainerContainerConnectionDetails(source);
  }

  private static final class KeycloakContainerContainerConnectionDetails extends ContainerConnectionDetails<KeycloakContainer>
    implements KeycloakConnectionDetails {

    private KeycloakContainerContainerConnectionDetails(ContainerConnectionSource<KeycloakContainer> source) {
      super(source);
    }

    @Override
    public String getAuthServerUrl() {
      return String.format("http://%s:%d", getContainer().getHost(),
        getContainer().getMappedPort(8080));
    }

    @Override
    public String getRealm() {
      return getContainer().getRealm();
    }

    @Override
    public String getClientId() {
      return getContainer().getClientId();
    }

    @Override
    public String getClientSecret() {
      return getContainer().getClientSecret();
    }

    @Override
    public String getIssuerUri() {
      return getAuthServerUrl() + "/realms/" + getRealm();
    }

    @Override
    public String getJwkSetUri() {
      return getIssuerUri() + "/protocol/openid-connect/certs";
    }

    @Override
    public JwtDecoder getJwtDecoder() {
      return null;
    }
  }
}
