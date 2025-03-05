package de.rieckpil.library.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "book_reviews")
public class BookReview {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private LibraryUser user;

  private Integer rating;

  @Column(columnDefinition = "TEXT")
  private String comment;

  @CreationTimestamp
  @Column(updatable = false)
  private ZonedDateTime createdAt;

  UUID getId() {
    return id;
  }

  void setId(UUID id) {
    this.id = id;
  }

  Book getBook() {
    return book;
  }

  void setBook(Book book) {
    this.book = book;
  }

  LibraryUser getUser() {
    return user;
  }

  void setUser(LibraryUser user) {
    this.user = user;
  }

  Integer getRating() {
    return rating;
  }

  void setRating(Integer rating) {
    this.rating = rating;
  }

  String getComment() {
    return comment;
  }

  void setComment(String comment) {
    this.comment = comment;
  }

  ZonedDateTime getCreatedAt() {
    return createdAt;
  }

  void setCreatedAt(ZonedDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
