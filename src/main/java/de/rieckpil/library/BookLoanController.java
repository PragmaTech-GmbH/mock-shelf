package de.rieckpil.library;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import de.rieckpil.library.model.Book;
import de.rieckpil.library.model.BookLoan;
import de.rieckpil.library.model.LibraryLocation;
import de.rieckpil.library.model.LibraryUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/loans")
public class BookLoanController {

  private final BookLoanService loanService;
  private final BookService bookService;
  private final UserService userService;
  private final LibraryLocationService locationService;

  public BookLoanController(
      BookLoanService loanService,
      BookService bookService,
      UserService userService,
      LibraryLocationService locationService) {
    this.loanService = loanService;
    this.bookService = bookService;
    this.userService = userService;
    this.locationService = locationService;
  }

  /** Display user's active loans */
  @GetMapping("/my-loans")
  @PreAuthorize("isAuthenticated()")
  public String myLoans(Model model) {
    LibraryUser currentUser = userService.getCurrentUser();

    List<BookLoan> activeLoans = loanService.getActiveLoansForUser(currentUser);
    List<BookLoan> loanHistory = loanService.getLoanHistoryForUser(currentUser, 5);

    model.addAttribute("activeLoans", activeLoans);
    model.addAttribute("loanHistory", loanHistory);
    model.addAttribute("user", currentUser);

    return "loans/my-loans";
  }

  /** Load more loan history with HTMX */
  @GetMapping("/history")
  @PreAuthorize("isAuthenticated()")
  public String loadMoreHistory(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      Model model) {

    LibraryUser currentUser = userService.getCurrentUser();

    List<BookLoan> loanHistory = loanService.getLoanHistoryForUser(currentUser, size, page);
    model.addAttribute("loanHistory", loanHistory);

    return "loans/fragments :: loan-history";
  }

  /** Display loan form for a book */
  @GetMapping("/new/form")
  @PreAuthorize("isAuthenticated()")
  public String showLoanForm(@RequestParam UUID bookId, Model model) {
    Book book = bookService.getBookById(bookId);

    if (!book.getAvailable()) {
      model.addAttribute("error", "This book is not available for loan");
      model.addAttribute("book", book);
      return "loans/fragments :: loan-error";
    }

    List<LibraryLocation> locations = locationService.getAllLocations();

    model.addAttribute("book", book);
    model.addAttribute("locations", locations);

    return "loans/fragments :: loan-form";
  }

  /** Process loan request */
  @PostMapping("/create")
  @PreAuthorize("isAuthenticated()")
  public String createLoan(
      @RequestParam UUID bookId,
      @RequestParam UUID pickupLocationId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate loanDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
      Model model) {

    try {
      LibraryUser currentUser = userService.getCurrentUser();

      Book book = bookService.getBookById(bookId);
      LibraryLocation location = locationService.getLocationById(pickupLocationId);

      BookLoan loan =
          loanService.createLoan(
              book,
              currentUser,
              location,
              ZonedDateTime.now(),
              ZonedDateTime.from(dueDate.atStartOfDay(ZonedDateTime.now().getZone())));

      model.addAttribute("loan", loan);
      return "loans/fragments :: loan-success";

    } catch (BookNotAvailableException e) {
      model.addAttribute("error", e.getMessage());
      return "loans/fragments :: loan-error";
    } catch (Exception e) {
      model.addAttribute("error", "An error occurred: " + e.getMessage());
      return "loans/fragments :: loan-error";
    }
  }

  /** Return a book */
  @PostMapping("/{id}/return")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public String returnBook(@PathVariable UUID id, Model model) {
    try {
      BookLoan loan = loanService.returnLoan(id);
      model.addAttribute("loan", loan);
      return "loans/fragments :: return-success";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "loans/fragments :: return-error";
    }
  }

  /** Extend a loan */
  @PostMapping("/{id}/extend")
  @PreAuthorize("isAuthenticated()")
  public String extendLoan(
      @PathVariable UUID id,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDueDate,
      Model model) {

    try {
      BookLoan loan =
          loanService.extendLoan(
              id, ZonedDateTime.from(newDueDate.atStartOfDay(ZonedDateTime.now().getZone())));

      model.addAttribute("loan", loan);
      return "loans/fragments :: extend-success";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "loans/fragments :: extend-error";
    }
  }

  /** Show loan details (admin) */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public String viewLoan(@PathVariable UUID id, Model model) {
    BookLoan loan = loanService.getLoanById(id);
    model.addAttribute("loan", loan);
    return "loans/details";
  }

  /** List all loans (admin) */
  @GetMapping
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public String listLoans(
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      Model model) {

    List<BookLoan> loans;

    if (status != null && !status.isEmpty()) {
      loans = loanService.getLoansByStatus(BookLoan.LoanStatus.valueOf(status.toUpperCase()));
    } else {
      loans = loanService.getAllLoans(page, size);
    }

    model.addAttribute("loans", loans);
    model.addAttribute("statuses", BookLoan.LoanStatus.values());
    model.addAttribute("selectedStatus", status);

    return "loans/list";
  }

  /** Filter loans with HTMX */
  @GetMapping("/filter")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public String filterLoans(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String userQuery,
      @RequestParam(required = false) String bookQuery,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      Model model) {

    List<BookLoan> filteredLoans =
        loanService.searchLoans(status, userQuery, bookQuery, page, size);
    model.addAttribute("loans", filteredLoans);

    return "loans/fragments :: loan-table";
  }
}
