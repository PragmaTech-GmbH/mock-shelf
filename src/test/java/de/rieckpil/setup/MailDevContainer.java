package de.rieckpil.setup;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class MailDevContainer extends GenericContainer<MailDevContainer> {

  private static final int SMTP_PORT = 1025;
  private static final int WEB_PORT = 1080;

  public MailDevContainer(String dockerImageName) {
    super(DockerImageName.parse(dockerImageName));

    withExposedPorts(SMTP_PORT, WEB_PORT)
        .withEnv("MAILDEV_INCOMING_USER", "user")
        .withEnv("MAILDEV_INCOMING_PASS", "password");
  }

  public String getSmtpHost() {
    return getHost();
  }

  public int getSmtpPort() {
    return getMappedPort(SMTP_PORT);
  }

  public String getWebUrl() {
    return String.format("http://%s:%d", getHost(), getMappedPort(WEB_PORT));
  }
}
