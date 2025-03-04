package de.rieckpil.library.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "books")
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String isbn;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String author;

  private String publisher;

  private LocalDate publicationDate;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String coverImageUrl;

  private Integer pageCount;

  private String language;

  private String category;

  private Boolean available = true;

  @CreationTimestamp
  @Column(updatable = false)
  private ZonedDateTime createdAt;

  @UpdateTimestamp private ZonedDateTime updatedAt;
}
