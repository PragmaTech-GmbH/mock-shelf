package de.rieckpil.library.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "book_loans")
public class BookLoan {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private LibraryUser user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pickup_location_id")
  private LibraryLocation pickupLocation;

  @Column(updatable = false)
  private ZonedDateTime loanDate;

  @NotNull private ZonedDateTime dueDate;

  private ZonedDateTime returnDate;

  @Enumerated(EnumType.STRING)
  private LoanStatus status = LoanStatus.ACTIVE;

  @CreationTimestamp
  @Column(updatable = false)
  private ZonedDateTime createdAt;

  @UpdateTimestamp private ZonedDateTime updatedAt;

  public enum LoanStatus {
    ACTIVE,
    RETURNED,
    OVERDUE,
    RESERVED
  }

  public boolean isOverdue() {
    return returnDate == null
        && ZonedDateTime.now().isAfter(dueDate)
        && status != LoanStatus.RETURNED;
  }

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

  LibraryLocation getPickupLocation() {
    return pickupLocation;
  }

  void setPickupLocation(LibraryLocation pickupLocation) {
    this.pickupLocation = pickupLocation;
  }

  ZonedDateTime getLoanDate() {
    return loanDate;
  }

  void setLoanDate(ZonedDateTime loanDate) {
    this.loanDate = loanDate;
  }

  ZonedDateTime getDueDate() {
    return dueDate;
  }

  void setDueDate(ZonedDateTime dueDate) {
    this.dueDate = dueDate;
  }

  ZonedDateTime getReturnDate() {
    return returnDate;
  }

  void setReturnDate(ZonedDateTime returnDate) {
    this.returnDate = returnDate;
  }

  LoanStatus getStatus() {
    return status;
  }

  void setStatus(LoanStatus status) {
    this.status = status;
  }

  ZonedDateTime getCreatedAt() {
    return createdAt;
  }

  void setCreatedAt(ZonedDateTime createdAt) {
    this.createdAt = createdAt;
  }

  ZonedDateTime getUpdatedAt() {
    return updatedAt;
  }

  void setUpdatedAt(ZonedDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
