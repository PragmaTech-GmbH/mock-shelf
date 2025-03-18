package de.rieckpil;

import de.rieckpil.library.setup.KeycloakContainer;
import de.rieckpil.library.setup.MailDevContainer;
import de.rieckpil.library.setup.WireMockContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.testcontainers.utility.DockerImageName.parse;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  ArtemisContainer artemisContainer() {
    return new ArtemisContainer(parse("rmohr/activemq:5.15.9").asCompatibleSubstituteFor("apache/activemq-artemis"))
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
    return new KeycloakContainer();
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
