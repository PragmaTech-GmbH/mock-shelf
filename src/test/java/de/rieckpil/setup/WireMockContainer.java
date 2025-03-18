package de.rieckpil.setup;

import java.time.Duration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class WireMockContainer extends GenericContainer<WireMockContainer> {

  private static final int HTTP_PORT = 8080;

  public WireMockContainer() {
    super(DockerImageName.parse("wiremock/wiremock:2.35.0"));

    withExposedPorts(HTTP_PORT)
      .withCommand("--verbose", "--global-response-templating")
      .waitingFor(Wait.forHttp("/__admin").forPort(HTTP_PORT))
      .withStartupTimeout(Duration.ofSeconds(30));
  }

  public WireMockContainer withMappings(String mappingsPath) {
    if (mappingsPath != null) {
      withFileSystemBind(mappingsPath, "/home/wiremock/mappings");
    }
    return this;
  }

  public WireMockContainer withFiles(String filesPath) {
    if (filesPath != null) {
      withFileSystemBind(filesPath, "/home/wiremock/__files");
    }
    return this;
  }

  public String getBaseUrl() {
    return String.format("http://%s:%d", getHost(), getMappedPort(HTTP_PORT));
  }
}
