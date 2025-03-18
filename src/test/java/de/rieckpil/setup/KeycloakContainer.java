package de.rieckpil.setup;

import java.time.Duration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class KeycloakContainer extends GenericContainer<KeycloakContainer> {

  private static final int HTTP_PORT = 8080;
  private String realm = "mockshelf";

  public KeycloakContainer() {
    super(DockerImageName.parse("quay.io/keycloak/keycloak:26.1"));

    withExposedPorts(HTTP_PORT)
      .withEnv("KEYCLOAK_ADMIN", "admin")
      .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
      .withEnv("KC_DB", "dev-file")
      .withEnv("KC_HEALTH_ENABLED", "true")
      .withCommand("start-dev")
      .waitingFor(Wait.forHttp("/health/ready").forPort(HTTP_PORT))
      .withStartupTimeout(Duration.ofMinutes(2));
  }

  public KeycloakContainer withRealmImport(String realmJsonPath) {
    if (realmJsonPath != null) {
      withCommand("start-dev", "--import-realm")
        .withFileSystemBind(realmJsonPath, "/opt/keycloak/data/import/realm.json");
    }
    return this;
  }

  public KeycloakContainer withRealm(String realm) {
    this.realm = realm;
    return this;
  }

  public String getRealm() {
    return realm;
  }

  public String getAuthServerUrl() {
    return String.format("http://%s:%d", getHost(), getMappedPort(HTTP_PORT));
  }

  public String getIssuerUrl() {
    return String.format("%s/realms/%s", getAuthServerUrl(), realm);
  }
}
