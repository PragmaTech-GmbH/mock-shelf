package de.rieckpil;

import de.rieckpil.library.setup.KeycloakContainer;
import de.rieckpil.library.setup.MailDevContainer;
import de.rieckpil.library.setup.WireMockContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import static org.testcontainers.utility.DockerImageName.parse;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

  @Bean
  ArtemisContainer artemisContainer() {
    return new ArtemisContainer(parse("apache/activemq-artemis:2.40.0"))
      .withExposedPorts(61616, 8161)
      .withPassword("activemq")
      .withUser("activemq");
  }

  @Bean
  @ServiceConnection
  PostgreSQLContainer<?> postgresContainer() {
    return new PostgreSQLContainer<>(parse("postgres:15"))
      .withDatabaseName("mock-shelf")
      .withUsername("postgres")
      .withPassword("postgres");
  }

  @Bean
  @ServiceConnection
  KeycloakContainer keycloakContainer() {
    return new KeycloakContainer()
      .withRealm("spring")
      .withRealmImport(MountableFile.forClasspathResource("/docker/keycloak/spring-realm.json"));
  }

  @Bean
  public DynamicPropertyRegistrar keycloakProperties(KeycloakContainer container) {
    return (properties) -> {
      properties.add("spring.security.oauth2.client.provider.keycloak.issuer-uri", container::getIssuerUrl);
      properties.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", container::getIssuerUrl);
    };
  }

  @Bean
  @ServiceConnection
  WireMockContainer wireMockContainer() {
    return new WireMockContainer();
  }

  @Bean
  @ServiceConnection
  MailDevContainer mailDevContainer() {
    return new MailDevContainer();
  }
}
