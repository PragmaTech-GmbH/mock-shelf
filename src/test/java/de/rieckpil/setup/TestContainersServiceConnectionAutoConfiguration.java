package de.rieckpil.setup;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.reactive.function.client.WebClient;

@AutoConfiguration
public class TestContainersServiceConnectionAutoConfiguration {

  /**
   * Configuration for Keycloak connection factories.
   */
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass({KeycloakContainer.class, JwtDecoder.class})
  static class KeycloakConnectionConfiguration {

    /**
     * Registers the Keycloak connection details factory.
     */
    @Bean
    KeycloakContainerConnectionDetailsFactory keycloakContainerConnectionDetailsFactory() {
      return new KeycloakContainerConnectionDetailsFactory();
    }

//    /**
//     * Provides a JWT decoder configured from the Keycloak connection.
//     */
//    @Bean
//    JwtDecoder jwtDecoder(KeycloakConnectionDetails connectionDetails) {
//      return connectionDetails.getJwtDecoder();
//    }
  }

  /**
   * Configuration for MailDev connection factories.
   */
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass({MailDevContainer.class, JavaMailSender.class})
  static class MailDevConnectionConfiguration {

    /**
     * Registers the MailDev connection details factory.
     */
    @Bean
    MailDevContainerConnectionDetailsFactory mailDevContainerConnectionDetailsFactory() {
      return new MailDevContainerConnectionDetailsFactory();
    }

    /**
     * Provides a JavaMailSender configured from the MailDev connection.
     */
//    @Bean
//    JavaMailSender mailSender(MailDevConnectionDetails connectionDetails) {
//      JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//      mailSender.setHost(connectionDetails.getHost());
//      mailSender.setPort(connectionDetails.getPort());
//      mailSender.setUsername(connectionDetails.getUsername());
//      mailSender.setPassword(connectionDetails.getPassword());
//
//      // Set additional properties
//      mailSender.getJavaMailProperties().put("mail.transport.protocol", "smtp");
//      mailSender.getJavaMailProperties().put("mail.smtp.auth", "true");
//      mailSender.getJavaMailProperties().put("mail.smtp.starttls.enable", "false");
//
//      return mailSender;
//    }
  }

  /**
   * Configuration for WireMock connection factories.
   */
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass({WireMockContainer.class, WebClient.class})
  static class WireMockConnectionConfiguration {

    /**
     * Registers the WireMock connection details factory.
     */
    @Bean
    WireMockContainerConnectionDetailsFactory wireMockContainerConnectionDetailsFactory() {
      return new WireMockContainerConnectionDetailsFactory();
    }

    /**
     * Provides a WebClient configured to use the WireMock server.
     */
//    @Bean
//    WebClient webClient(WireMockConnectionDetails connectionDetails) {
//      return WebClient.builder()
//        .baseUrl(connectionDetails.getBaseUrl())
//        .build();
//    }
  }
}
