package de.rieckpil.setup;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

public class MailDevContainerConnectionDetailsFactory
    implements ConnectionDetailsFactory<
        ContainerConnectionSource<MailDevContainer>, MailDevConnectionDetails> {

  @Override
  public MailDevConnectionDetails getConnectionDetails(
      ContainerConnectionSource<MailDevContainer> source) {
    MailDevContainer container = new MailDevContainer("");
    return new MailDevConnectionDetails() {

      @Override
      public String getHost() {
        return container.getSmtpHost();
      }

      @Override
      public int getSmtpPort() {
        return container.getSmtpPort();
      }

      @Override
      public String getWebUrl() {
        return container.getWebUrl();
      }

      @Override
      public String getUsername() {
        return "user";
      }

      @Override
      public String getPassword() {
        return "password";
      }
    };
  }
}
