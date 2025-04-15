package de.rieckpil.library.setup;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class KeycloakContainer extends GenericContainer<KeycloakContainer> {

  private static final Logger LOG = LoggerFactory.getLogger(KeycloakContainer.class);

  private static final int HTTP_PORT = 8080;
  private static final int MANAGEMENT_PORT = 9000;
  private String realm = "spring";

  public KeycloakContainer() {
    super(DockerImageName.parse("quay.io/keycloak/keycloak:26.1"));

    withExposedPorts(HTTP_PORT, MANAGEMENT_PORT)
      .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
      .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin")
      .withEnv("KC_DB", "dev-file")
      .withEnv("KC_HEALTH_ENABLED", "true")
      .waitingFor(Wait.forHttp("/health/ready").forPort(MANAGEMENT_PORT))
      .withLogConsumer(new Slf4jLogConsumer(LOG))
      .withStartupTimeout(Duration.ofMinutes(2));
  }

  public KeycloakContainer withRealmImport(MountableFile realmJsonPath) {
    if (realmJsonPath != null) {
      withCommand("start-dev", "--import-realm")
        .withCopyFileToContainer(realmJsonPath, "/opt/keycloak/data/import/realm.json");

      LOG.info("Using realm import file: {}", realmJsonPath);
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

  public String getClientId() {
    return "mock-shelf";
  }

  public String getClientSecret() {
    return "mock-shelf";
  }
}
