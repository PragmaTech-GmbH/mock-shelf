package de.rieckpil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.testcontainers.utility.DockerImageName.parse;

@ExtendWith(ContainerReuseExtension.class)
public class ReuseTest {

  static PostgreSQLContainer<?> postgresContainer =
      new PostgreSQLContainer<>(parse("postgres:15"))
          .withDatabaseName("mock-shelf")
          .withUsername("postgres")
          .withPassword("postgres");

  static {
    postgresContainer.start();
  }

  @Test
  void foo() throws Exception {
    System.out.println(postgresContainer.isShouldBeReused());
  }

  @Test
  void bar() throws Exception {
    System.out.println(postgresContainer.isShouldBeReused());
  }
}
