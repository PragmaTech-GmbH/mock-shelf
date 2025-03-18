package de.rieckpil.setup;

import java.time.Duration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class MailDevContainer extends GenericContainer<MailDevContainer> {

  private static final int SMTP_PORT = 1025;
  private static final int WEB_PORT = 1080;

  public MailDevContainer() {
    super(DockerImageName.parse("maildev/maildev:2.0.5"));

    withExposedPorts(SMTP_PORT, WEB_PORT)
      .withEnv("MAILDEV_INCOMING_USER", "user")
      .withEnv("MAILDEV_INCOMING_PASS", "password")
      .waitingFor(Wait.forHttp("/").forPort(WEB_PORT))
      .withStartupTimeout(Duration.ofSeconds(30));
  }

  public int getSmtpPort() {
    return getMappedPort(SMTP_PORT);
  }

  public int getWebPort() {
    return getMappedPort(WEB_PORT);
  }

  public String getWebUrl() {
    return String.format("http://%s:%d", getHost(), getWebPort());
  }
}
