package de.rieckpil.library.setup;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

public class MailDevContainerConnectionDetailsFactory
  extends ContainerConnectionDetailsFactory<MailDevContainer, MailDevConnectionDetails> {

  @Override
  protected MailDevConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<MailDevContainer> source) {
    MailDevContainer container = null;
    MailProperties properties = new MailProperties();

    properties.setHost(container.getHost());
    properties.setPort(container.getSmtpPort());
    properties.setUsername("user");
    properties.setPassword("password");

    // Set additional properties as needed
    properties.getProperties().put("mail.smtp.auth", "true");
    properties.getProperties().put("mail.smtp.starttls.enable", "false");

    return new MailDevConnectionDetails() {
      @Override
      public String getHost() {
        return "";
      }

      @Override
      public int getSmtpPort() {
        return 0;
      }

      @Override
      public String getWebUrl() {
        return "";
      }

      @Override
      public String getUsername() {
        return "";
      }

      @Override
      public String getPassword() {
        return "";
      }
    };
  }
}
