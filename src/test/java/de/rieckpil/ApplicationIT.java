package de.rieckpil;

import de.rieckpil.library.setup.KeycloakConnectionDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ApplicationIT {

  @Autowired private KeycloakConnectionDetails connectionDetails;

  @Test
  void contextLoads() {
    assertNotNull(connectionDetails);
  }
}
