package de.rieckpil.library.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "library_users")
public class LibraryUser {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String keycloakId;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @Column(unique = true, nullable = false)
  private String email;

  private String phone;

  @Column(columnDefinition = "TEXT")
  private String address;

  @CreationTimestamp
  @Column(updatable = false)
  private ZonedDateTime registeredAt;

  private Boolean isAdmin = false;

  public String getFullName() {
    return firstName + " " + lastName;
  }

  public UUID getId() {
    return id;
  }

  void setId(UUID id) {
    this.id = id;
  }

  String getKeycloakId() {
    return keycloakId;
  }

  void setKeycloakId(String keycloakId) {
    this.keycloakId = keycloakId;
  }

  String getFirstName() {
    return firstName;
  }

  void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  String getLastName() {
    return lastName;
  }

  void setLastName(String lastName) {
    this.lastName = lastName;
  }

  String getEmail() {
    return email;
  }

  void setEmail(String email) {
    this.email = email;
  }

  String getPhone() {
    return phone;
  }

  void setPhone(String phone) {
    this.phone = phone;
  }

  String getAddress() {
    return address;
  }

  void setAddress(String address) {
    this.address = address;
  }

  ZonedDateTime getRegisteredAt() {
    return registeredAt;
  }

  void setRegisteredAt(ZonedDateTime registeredAt) {
    this.registeredAt = registeredAt;
  }

  Boolean getAdmin() {
    return isAdmin;
  }

  void setAdmin(Boolean admin) {
    isAdmin = admin;
  }
}
