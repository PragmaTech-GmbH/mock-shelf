package de.rieckpil.library.setup;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface WireMockConnectionDetails extends ConnectionDetails {
  /**
   * Gets the base URL for the WireMock API.
   *
   * @return the base URL
   */
  String getBaseUrl();
}
