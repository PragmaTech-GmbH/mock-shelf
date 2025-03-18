package de.rieckpil.library.setup;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface MailDevConnectionDetails extends ConnectionDetails {
  String getHost();

  int getSmtpPort();

  String getWebUrl();

  String getUsername();

  String getPassword();
}
