package de.rieckpil.library;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import de.rieckpil.library.model.Book;
import de.rieckpil.library.model.BookLoan;
import de.rieckpil.library.model.LibraryLocation;
import de.rieckpil.library.model.LibraryUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/loans")
public class BookLoanController {

  private final BookLoanService loanService;
  private final BookService bookService;
  private final UserService userService;
  private final LibraryLocationService locationService;

  private static final Logger LOG = LoggerFactory.getLogger(BookLoanController.class);

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
  @GetMapping("/new")
  @PreAuthorize("isAuthenticated()")
  public String showLoanForm(@RequestParam UUID bookId, Model model) {
    Book book = bookService.getBookById(bookId);

    if (!book.getAvailable()) {
      return "redirect:/books/" + bookId + "?error=not_available";
    }

    List<LibraryLocation> locations = locationService.getAllLocations();

    model.addAttribute("book", book);
    model.addAttribute("locations", locations);
    model.addAttribute("loanDuration", 14); // Default to 14 days

    return "loans/create";
  }

  /** Display HTMX loan form for a book */
  @GetMapping(value = "/new/form", produces = MediaType.TEXT_HTML_VALUE)
  @PreAuthorize("isAuthenticated()")
  public String showLoanFormHtmx(@RequestParam UUID bookId, Model model) {
    Book book = bookService.getBookById(bookId);

    if (!book.getAvailable()) {
      model.addAttribute("error", "This book is not available for loan");
      model.addAttribute("book", book);
      return "loans/fragments :: loan-error";
    }

    List<LibraryLocation> locations = locationService.getAllLocations();

    model.addAttribute("book", book);
    model.addAttribute("locations", locations);
    model.addAttribute("loanDuration", 14); // Default to 14 days

    return "loans/fragments :: loan-form";
  }

  /** Get loan due date preview with HTMX */
  @GetMapping(value = "/due-date-preview", produces = MediaType.TEXT_HTML_VALUE)
  @PreAuthorize("isAuthenticated()")
  public String getDueDatePreview(
      @RequestParam int loanDays,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      Model model) {

    // Default to today if no date is provided
    LocalDate loanStartDate = startDate != null ? startDate : LocalDate.now();

    // Calculate due date
    LocalDate dueDate = loanStartDate.plusDays(loanDays);

    model.addAttribute("dueDate", dueDate);
    model.addAttribute("loanDays", loanDays);

    return "loans/fragments :: due-date-preview";
  }

  @PostMapping("/create")
  @PreAuthorize("isAuthenticated()")
  public String createLoan(
      @RequestParam UUID bookId,
      @RequestParam UUID pickupLocationId,
      @RequestParam(defaultValue = "14") int loanDays,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate loanDate,
      Model model,
      RedirectAttributes redirectAttributes) {

    try {
      // Get current user, handle null case
      LibraryUser currentUser = userService.getCurrentUser();
      if (currentUser == null) {
        LOG.error("Failed to get current user when creating loan");
        redirectAttributes.addFlashAttribute(
            "errorMessage", "Authentication error. Please log out and try again.");
        return "redirect:/books/" + bookId;
      }

      LOG.info(
          "Creating loan for user {} (ID: {})", currentUser.getFullName(), currentUser.getId());

      Book book = bookService.getBookById(bookId);
      LibraryLocation location = locationService.getLocationById(pickupLocationId);

      // Default to today if no date is provided
      LocalDate loanStartDate = loanDate != null ? loanDate : LocalDate.now();

      // Calculate due date
      ZonedDateTime loanStartDateTime = loanStartDate.atStartOfDay(ZonedDateTime.now().getZone());
      ZonedDateTime dueDateTime = loanStartDateTime.plusDays(loanDays);

      BookLoan loan =
          loanService.createLoan(book, currentUser, location, loanStartDateTime, dueDateTime);

      redirectAttributes.addFlashAttribute(
          "successMessage",
          "Book '"
              + book.getTitle()
              + "' has been successfully borrowed. Please return it by "
              + dueDateTime.toLocalDate().toString()
              + ".");

      return "redirect:/loans/my-loans";

    } catch (BookNotAvailableException e) {
      LOG.warn("Book not available: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/books/" + bookId;
    } catch (Exception e) {
      LOG.error("Error creating loan: ", e);
      redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
      return "redirect:/books/" + bookId;
    }
  }

  // HTMX version follows the same pattern
  @PostMapping("/create/htmx")
  @PreAuthorize("isAuthenticated()")
  public String createLoanHtmx(
      @RequestParam UUID bookId,
      @RequestParam UUID pickupLocationId,
      @RequestParam(defaultValue = "14") int loanDays,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate loanDate,
      Model model) {

    try {
      // Get current user, handle null case
      LibraryUser currentUser = userService.getCurrentUser();
      if (currentUser == null) {
        LOG.error("Failed to get current user when creating loan via HTMX");
        model.addAttribute("error", "Authentication error. Please log out and try again.");
        return "loans/fragments :: loan-error";
      }

      Book book = bookService.getBookById(bookId);
      LibraryLocation location = locationService.getLocationById(pickupLocationId);

      // Default to today if no date is provided
      LocalDate loanStartDate = loanDate != null ? loanDate : LocalDate.now();

      // Calculate due date
      ZonedDateTime loanStartDateTime = loanStartDate.atStartOfDay(ZonedDateTime.now().getZone());
      ZonedDateTime dueDateTime = loanStartDateTime.plusDays(loanDays);

      BookLoan loan =
          loanService.createLoan(book, currentUser, location, loanStartDateTime, dueDateTime);

      model.addAttribute("loan", loan);
      return "loans/fragments :: loan-success";

    } catch (BookNotAvailableException e) {
      model.addAttribute("error", e.getMessage());
      return "loans/fragments :: loan-error";
    } catch (Exception e) {
      LOG.error("Error creating loan via HTMX: ", e);
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
