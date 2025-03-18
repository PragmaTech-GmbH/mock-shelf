package de.rieckpil.setup;

import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

public class WireMockContainerConnectionDetailsFactory
  extends ContainerConnectionDetailsFactory<WireMockContainer, WireMockConnectionDetails> {

  @Override
  protected WireMockConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<WireMockContainer> source) {
    WireMockContainer container = null;
    String baseUrl = String.format("http://%s:%d", container.getHost(), container.getMappedPort(8080));

    return new WireMockConnectionDetails() {
      @Override
      public String getBaseUrl() {
        return baseUrl;
      }
    };
  }
}
