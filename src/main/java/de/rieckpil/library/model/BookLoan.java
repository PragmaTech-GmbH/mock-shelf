package de.rieckpil.library.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Book getBook() {
    return book;
  }

  public void setBook(Book book) {
    this.book = book;
  }

  public LibraryUser getUser() {
    return user;
  }

  public void setUser(LibraryUser user) {
    this.user = user;
  }

  public LibraryLocation getPickupLocation() {
    return pickupLocation;
  }

  public void setPickupLocation(LibraryLocation pickupLocation) {
    this.pickupLocation = pickupLocation;
  }

  public ZonedDateTime getLoanDate() {
    return loanDate;
  }

  public void setLoanDate(ZonedDateTime loanDate) {
    this.loanDate = loanDate;
  }

  public ZonedDateTime getDueDate() {
    return dueDate;
  }

  public void setDueDate(ZonedDateTime dueDate) {
    this.dueDate = dueDate;
  }

  public ZonedDateTime getReturnDate() {
    return returnDate;
  }

  public void setReturnDate(ZonedDateTime returnDate) {
    this.returnDate = returnDate;
  }

  public LoanStatus getStatus() {
    return status;
  }

  public void setStatus(LoanStatus status) {
    this.status = status;
  }

  public ZonedDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(ZonedDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public ZonedDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(ZonedDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
