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
}
