package de.rieckpil.setup;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.autoconfigure.service.connection.ConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.testcontainers.containers.GenericContainer;

public class MailDevConnectionDetailsFactory
@Override
public KeycloakConnectionDetails getConnectionDetails(ContainerConnectionSource<GenericContainer<?>> source) {

}
  @Override
  public MailProperties getConnectionDetails(ContainerConnectionSource<MailDevContainer> source) {
    MailDevContainer container = source.getContainer();
    MailProperties properties = new MailProperties();

    properties.setHost(container.getHost());
    properties.setPort(container.getSmtpPort());
    properties.setUsername("user");
    properties.setPassword("password");

    // Set additional properties as needed
    properties.getProperties().put("mail.smtp.auth", "true");
    properties.getProperties().put("mail.smtp.starttls.enable", "false");

    return properties;
  }
}
