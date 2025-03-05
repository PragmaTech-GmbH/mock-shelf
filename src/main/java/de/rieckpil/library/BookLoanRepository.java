package de.rieckpil.library;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.rieckpil.library.model.Book;
import de.rieckpil.library.model.BookLoan;
import de.rieckpil.library.model.LibraryUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookLoanRepository extends JpaRepository<BookLoan, UUID> {

  /** Find all loans for a specific user */
  List<BookLoan> findByUser(LibraryUser user);

  /** Find all loans for a specific book */
  List<BookLoan> findByBook(Book book);

  /** Find all loans with a specific status */
  List<BookLoan> findByStatus(BookLoan.LoanStatus status);

  /** Find active loans for a specific user */
  @Query("SELECT l FROM BookLoan l WHERE l.user = :user AND l.status IN ('ACTIVE', 'OVERDUE')")
  List<BookLoan> findActiveLoans(@Param("user") LibraryUser user);

  /** Find if a book is currently on loan (active or overdue) */
  @Query("SELECT l FROM BookLoan l WHERE l.book = :book AND l.status IN ('ACTIVE', 'OVERDUE')")
  Optional<BookLoan> findActiveBookLoan(@Param("book") Book book);

  /** Find all overdue loans (due date is in the past and status is ACTIVE) */
  @Query("SELECT l FROM BookLoan l WHERE l.dueDate < :now AND l.status = 'ACTIVE'")
  List<BookLoan> findOverdueLoans(@Param("now") ZonedDateTime now);

  /** Find loans due between two dates */
  @Query(
      "SELECT l FROM BookLoan l WHERE l.dueDate BETWEEN :fromDate AND :toDate AND l.status = 'ACTIVE'")
  List<BookLoan> findLoansDueBetween(
      @Param("fromDate") ZonedDateTime fromDate, @Param("toDate") ZonedDateTime toDate);

  /** Count active loans for statistics */
  @Query("SELECT COUNT(l) FROM BookLoan l WHERE l.status = 'ACTIVE'")
  Long countActiveLoans();

  /** Count overdue loans for statistics */
  @Query("SELECT COUNT(l) FROM BookLoan l WHERE l.status = 'OVERDUE'")
  Long countOverdueLoans();

  /** Find loans that will be due in the next N days */
  @Query("SELECT l FROM BookLoan l WHERE l.dueDate BETWEEN :start AND :end AND l.status = 'ACTIVE'")
  List<BookLoan> findLoansDueInNextDays(
      @Param("start") ZonedDateTime start, @Param("end") ZonedDateTime end);

  /** Count loans created in the last month for statistics */
  @Query(
      value = "SELECT COUNT(*) FROM book_loans WHERE created_at >= now() - interval '30 days'",
      nativeQuery = true)
  Long countLoansInLastMonth();

  /** Find loans for statistics dashboard */
  @Query("SELECT l FROM BookLoan l WHERE l.user.id = :userId ORDER BY l.loanDate DESC")
  List<BookLoan> findLoanHistoryByUser(@Param("userId") UUID userId);

  /** Find the most recently borrowed books */
  @Query(
      "SELECT l.book, COUNT(l) as loanCount FROM BookLoan l "
          + "GROUP BY l.book ORDER BY loanCount DESC")
  List<Object[]> findMostBorrowedBooks(Pageable pageable);

  /** Check if a user has reached their maximum allowed loans */
  @Query(
      "SELECT COUNT(l) FROM BookLoan l WHERE l.user = :user AND l.status IN ('ACTIVE', 'OVERDUE')")
  Long countActiveLoansForUser(@Param("user") LibraryUser user);

  /**
   * Find all loans that should have reminder emails sent (due in reminderDays and no notification
   * sent yet)
   */
  @Query(
      "SELECT l FROM BookLoan l WHERE l.dueDate BETWEEN :reminderStart AND :reminderEnd "
          + "AND l.status = 'ACTIVE' "
          + "AND NOT EXISTS (SELECT n FROM Notification n WHERE n.loan = l AND n.type = 'EMAIL')")
  List<BookLoan> findLoansNeedingReminders(
      @Param("reminderStart") ZonedDateTime reminderStart,
      @Param("reminderEnd") ZonedDateTime reminderEnd);
}
