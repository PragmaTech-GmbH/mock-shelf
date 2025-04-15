package de.rieckpil.library;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import de.rieckpil.library.model.Book;
import de.rieckpil.library.model.BookLoan;
import de.rieckpil.library.model.LibraryLocation;
import de.rieckpil.library.model.LibraryUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookLoanService {

  private final BookLoanRepository bookLoanRepository;
  private final BookService bookService;

  private static final Logger LOG = LoggerFactory.getLogger(BookLoanService.class);

  // Maximum number of active loans a user can have
  private static final int MAX_ACTIVE_LOANS = 5;

  public BookLoanService(BookLoanRepository bookLoanRepository, BookService bookService) {
    this.bookLoanRepository = bookLoanRepository;
    this.bookService = bookService;
  }

  public Long countActiveLoans() {
    return bookLoanRepository.countActiveLoans();
  }

  public Long countOverdueLoans() {
    return bookLoanRepository.countOverdueLoans();
  }

  public List<BookLoan> getRecentLoans(int amount) {
    return bookLoanRepository
        .findAll(PageRequest.of(0, amount, Sort.by(Sort.Direction.DESC, "loanDate")))
        .getContent();
  }

  public List<BookLoan> getLoansForUser(LibraryUser user) {
    return bookLoanRepository.findByUser(user);
  }

  public List<BookLoan> getActiveLoansForUser(LibraryUser currentUser) {
    return bookLoanRepository.findActiveLoans(currentUser);
  }

  public List<BookLoan> getLoanHistoryForUser(LibraryUser currentUser, int amount) {
    return bookLoanRepository.findByUser(currentUser).stream()
        .sorted((a, b) -> b.getLoanDate().compareTo(a.getLoanDate()))
        .limit(amount)
        .toList();
  }

  public List<BookLoan> getLoanHistoryForUser(LibraryUser currentUser, int size, int page) {
    return bookLoanRepository.findByUser(currentUser).stream()
        .sorted((a, b) -> b.getLoanDate().compareTo(a.getLoanDate()))
        .skip((long) page * size)
        .limit(size)
        .toList();
  }

  @Transactional
  public BookLoan createLoan(
      Book book,
      LibraryUser currentUser,
      LibraryLocation location,
      ZonedDateTime loanDate,
      ZonedDateTime dueDate) {

    // Validate all inputs
    if (book == null) {
      throw new IllegalArgumentException("Book cannot be null");
    }
    if (currentUser == null) {
      throw new IllegalArgumentException("User cannot be null");
    }
    if (location == null) {
      throw new IllegalArgumentException("Location cannot be null");
    }

    LOG.info(
        "Creating loan for book '{}' (ID: {}) for user {} (ID: {})",
        book.getTitle(),
        book.getId(),
        currentUser.getFullName(),
        currentUser.getId());

    // Check if the book is available
    if (!book.getAvailable()) {
      throw new BookNotAvailableException("This book is not available for loan.");
    }

    // Create new loan
    BookLoan loan = new BookLoan();
    loan.setBook(book);
    loan.setUser(currentUser);
    loan.setPickupLocation(location);
    loan.setLoanDate(loanDate);
    loan.setDueDate(dueDate);
    loan.setStatus(BookLoan.LoanStatus.ACTIVE);

    // Update book availability
    book.setAvailable(false);
    bookService.updateAvailability(book.getId(), false);

    BookLoan savedLoan = bookLoanRepository.save(loan);
    LOG.info("Successfully created loan ID: {}", savedLoan.getId());

    return savedLoan;
  }

  @Transactional
  public BookLoan returnLoan(UUID id) {
    BookLoan loan = getLoanById(id);

    // Check if loan is already returned
    if (loan.getStatus() == BookLoan.LoanStatus.RETURNED) {
      throw new IllegalStateException("This book has already been returned.");
    }

    // Update loan status
    loan.setStatus(BookLoan.LoanStatus.RETURNED);
    loan.setReturnDate(ZonedDateTime.now());

    // Update book availability
    Book book = loan.getBook();
    bookService.updateAvailability(book.getId(), true);

    return bookLoanRepository.save(loan);
  }

  @Transactional
  public BookLoan extendLoan(UUID id, ZonedDateTime newDueDate) {
    BookLoan loan = getLoanById(id);

    // Check if loan can be extended
    if (loan.getStatus() != BookLoan.LoanStatus.ACTIVE
        && loan.getStatus() != BookLoan.LoanStatus.OVERDUE) {
      throw new IllegalStateException("This loan cannot be extended.");
    }

    // Check if new due date is valid (must be in the future and after current due date)
    ZonedDateTime now = ZonedDateTime.now();
    if (newDueDate.isBefore(now)) {
      throw new IllegalArgumentException("New due date must be in the future.");
    }

    if (newDueDate.isBefore(loan.getDueDate())) {
      throw new IllegalArgumentException("New due date must be later than the current due date.");
    }

    // Update due date
    loan.setDueDate(newDueDate);

    // If loan was overdue, set it back to active
    if (loan.getStatus() == BookLoan.LoanStatus.OVERDUE) {
      loan.setStatus(BookLoan.LoanStatus.ACTIVE);
    }

    return bookLoanRepository.save(loan);
  }

  public BookLoan getLoanById(UUID id) {
    return bookLoanRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Loan not found with ID: " + id));
  }

  public List<BookLoan> getLoansByStatus(BookLoan.LoanStatus loanStatus) {
    return bookLoanRepository.findByStatus(loanStatus);
  }

  public List<BookLoan> getAllLoans(int page, int size) {
    return bookLoanRepository
        .findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "loanDate")))
        .getContent();
  }

  public List<BookLoan> searchLoans(
      String status, String userQuery, String bookQuery, int page, int size) {
    // This would need a more sophisticated implementation with custom queries
    // For now, let's just return a filtered list based on status
    if (status != null && !status.isEmpty()) {
      return getLoansByStatus(BookLoan.LoanStatus.valueOf(status.toUpperCase()));
    }
    return getAllLoans(page, size);
  }

  @Transactional
  public void updateOverdueLoans() {
    ZonedDateTime now = ZonedDateTime.now();
    List<BookLoan> overdueLoans = bookLoanRepository.findOverdueLoans(now);

    for (BookLoan loan : overdueLoans) {
      loan.setStatus(BookLoan.LoanStatus.OVERDUE);
      bookLoanRepository.save(loan);
    }
  }
}
