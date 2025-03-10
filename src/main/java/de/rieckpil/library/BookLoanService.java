package de.rieckpil.library;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import de.rieckpil.library.model.Book;
import de.rieckpil.library.model.BookLoan;
import de.rieckpil.library.model.LibraryLocation;
import de.rieckpil.library.model.LibraryUser;
import org.springframework.stereotype.Service;

@Service
public class BookLoanService {
  Long countActiveLoans() {
    return null;
  }

  Long countOverdueLoans() {
    return null;
  }

  List<BookLoan> getRecentLoans(int amount) {
    return null;
  }

  List<BookLoan> getLoansForUser(LibraryUser user) {
    return null;
  }

  List<BookLoan> getActiveLoansForUser(LibraryUser currentUser) {
    return null;
  }

  List<BookLoan> getLoanHistoryForUser(LibraryUser currentUser, int amount) {
    return null;
  }

  List<BookLoan> getLoanHistoryForUser(LibraryUser currentUser, int size, int page) {
    return null;
  }

  BookLoan createLoan(
      Book book,
      LibraryUser currentUser,
      LibraryLocation location,
      ZonedDateTime now,
      ZonedDateTime from) {
    return null;
  }

  BookLoan returnLoan(UUID id) {
    return null;
  }

  BookLoan extendLoan(UUID id, ZonedDateTime from) {
    return null;
  }

  BookLoan getLoanById(UUID id) {
    return null;
  }

  List<BookLoan> getLoansByStatus(BookLoan.LoanStatus loanStatus) {
    return null;
  }

  List<BookLoan> getAllLoans(int page, int size) {
    return null;
  }

  List<BookLoan> searchLoans(
      String status, String userQuery, String bookQuery, int page, int size) {
    return null;
  }
}
