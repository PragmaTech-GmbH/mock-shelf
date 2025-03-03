package de.rieckpil;

import org.springframework.boot.SpringApplication;

public class TestMockShelfApplication {

  public static void main(String[] args) {
    SpringApplication.from(Application::main).with(TestcontainersConfiguration.class).run(args);
  }
}
