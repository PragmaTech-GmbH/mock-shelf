package de.rieckpil.setup;

import java.util.Properties;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TestPropertyBindingConfiguration {

  /**
   * Configures mail sender based on MailDev connection details.
   */
//  @Bean
//  @Primary
//  public JavaMailSender mailSender(MailDevConnectionDetails mailDevDetails) {
//    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//    mailSender.setHost(mailDevDetails.getHost());
//    mailSender.setPort(mailDevDetails.getSmtpPort());
//    mailSender.setUsername(mailDevDetails.getUsername());
//    mailSender.setPassword(mailDevDetails.getPassword());
//
//    Properties props = mailSender.getJavaMailProperties();
//    props.put("mail.transport.protocol", "smtp");
//    props.put("mail.smtp.auth", "true");
//    props.put("mail.smtp.starttls.enable", "false");
//    props.put("mail.debug", "true");
//
//    return mailSender;
//  }

  /**
   * Creates a WebClient for WireMock.
   */
//  @Bean
//  @Primary
//  public WebClient isbnApiWebClient(WireMockConnectionDetails wireMockDetails) {
//    return WebClient.builder()
//      .baseUrl(wireMockDetails.getBaseUrl())
//      .build();
//  }

  /**
   * Configures JWT decoder from Keycloak.
   */
  @Bean
  @Primary
  public JwtDecoder jwtDecoder(KeycloakConnectionDetails keycloakDetails) {
    return keycloakDetails.getJwtDecoder();
  }

  /**
   * Overrides OAuth2 resource server properties.
   */
  @Bean
  @Primary
  @ConfigurationProperties(prefix = "spring.security.oauth2.resourceserver.jwt")
  public OAuth2ResourceServerProperties oauth2ResourceServerProperties(KeycloakConnectionDetails keycloakDetails) {
    OAuth2ResourceServerProperties properties = new OAuth2ResourceServerProperties();
//    properties.setIssuerUri(keycloakDetails.getIssuerUrl());
    return properties;
  }
}
