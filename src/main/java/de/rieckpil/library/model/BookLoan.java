package de.rieckpil.library.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "book_loans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
  @Builder.Default
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
}
