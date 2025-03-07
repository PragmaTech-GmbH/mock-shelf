package de.rieckpil;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  ArtemisContainer artemisContainer() {
    return new ArtemisContainer(DockerImageName.parse("apache/activemq-artemis:5.15.9"))
        .withExposedPorts(61616, 8161)
        .withEnv("ACTIVEMQ_ADMIN_LOGIN", "admin")
        .withEnv("ACTIVEMQ_ADMIN_PASSWORD", "admin")
        .waitingFor(Wait.forHttp("/").forPort(8161));
  }

  @Bean
  @ServiceConnection
  PostgreSQLContainer<?> postgresContainer() {
    return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
        .withDatabaseName("mock-shelf")
        .withUsername("postgres")
        .withPassword("postgres");
  }

  @Bean
  @ServiceConnection
  GenericContainer<?> keycloakContainer() {
    return new GenericContainer<>(DockerImageName.parse("quay.io/keycloak/keycloak:22.0"))
        .withExposedPorts(8080)
        .withEnv("KEYCLOAK_ADMIN", "admin")
        .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
        .withEnv("KC_HEALTH_ENABLED", "true")
        .withCommand("start-dev", "--import-realm")
        .withFileSystemBind(
            "./docker/keycloak/mockshelf-realm.json",
            "/opt/keycloak/data/import/mockshelf-realm.json");
  }

  @Bean
  @ServiceConnection
  GenericContainer<?> wiremockContainer() {
    return new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:2.35.0"))
        .withExposedPorts(8080)
        .withFileSystemBind("./docker/wiremock/mappings", "/home/wiremock/mappings")
        .withFileSystemBind("./docker/wiremock/__files", "/home/wiremock/__files")
        .withCommand("--verbose", "--global-response-templating");
  }

  @Bean
  @ServiceConnection
  GenericContainer<?> maildevContainer() {
    return new GenericContainer<>(DockerImageName.parse("maildev/maildev:2.0.5"))
        .withExposedPorts(1025, 1080)
        .withEnv("MAILDEV_INCOMING_USER", "user")
        .withEnv("MAILDEV_INCOMING_PASS", "password");
  }
}
