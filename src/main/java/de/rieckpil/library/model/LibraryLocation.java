package de.rieckpil.library.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "library_locations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryLocation {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @NotBlank
  @Column(nullable = false)
  private String name;

  @NotBlank
  @Column(columnDefinition = "TEXT", nullable = false)
  private String address;

  private String phone;

  private String email;

  @Column(columnDefinition = "TEXT")
  private String openingHours;

  @CreationTimestamp
  @Column(updatable = false)
  private ZonedDateTime createdAt;
}
