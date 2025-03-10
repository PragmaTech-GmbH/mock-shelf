package de.rieckpil.library;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.rieckpil.library.model.Book;
import de.rieckpil.library.model.BookLoan;
import de.rieckpil.library.model.LibraryLocation;
import de.rieckpil.library.model.LibraryUser;
import de.rieckpil.library.model.Notification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

  private final BookService bookService;
  private final BookLoanService loanService;
  private final UserService userService;
  private final NotificationService notificationService;
  private final StatsService statsService;
  private final LibraryLocationService locationService;

  public AdminController(
      BookService bookService,
      BookLoanService loanService,
      UserService userService,
      NotificationService notificationService,
      StatsService statsService,
      LibraryLocationService locationService) {
    this.bookService = bookService;
    this.loanService = loanService;
    this.userService = userService;
    this.notificationService = notificationService;
    this.statsService = statsService;
    this.locationService = locationService;
  }

  /** Admin dashboard */
  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    // Get summary statistics
    Map<String, Long> statistics = new HashMap<>();
    statistics.put("totalBooks", bookService.countAllBooks());
    statistics.put("availableBooks", bookService.countAvailableBooks());
    statistics.put("totalUsers", userService.countAllUsers());
    statistics.put("activeLoans", loanService.countActiveLoans());
    statistics.put("overdueLoans", loanService.countOverdueLoans());

    // Recent activity
    List<Book> recentBooks = bookService.getRecentlyAddedBooks(5);
    List<BookLoan> recentLoans = loanService.getRecentLoans(5);
    List<LibraryUser> recentUsers = userService.getRecentlyRegisteredUsers(5);

    model.addAttribute("stats", statistics);
    model.addAttribute("recentBooks", recentBooks);
    model.addAttribute("recentLoans", recentLoans);
    model.addAttribute("recentUsers", recentUsers);

    return "admin/dashboard";
  }

  /** Load statistics data with HTMX */
  @GetMapping("/stats-data")
  public String getStatsData(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate,
      Model model) {

    // Default to last 30 days if dates not provided
    ZonedDateTime start =
        startDate != null
            ? startDate.atStartOfDay(ZonedDateTime.now().getZone())
            : ZonedDateTime.now().minusDays(30);

    ZonedDateTime end =
        endDate != null ? endDate.atStartOfDay(ZonedDateTime.now().getZone()) : ZonedDateTime.now();

    Map<String, Object> chartData = statsService.getActivityStatistics(start, end);
    model.addAttribute("chartData", chartData);

    return "admin/fragments :: stats-charts";
  }

  /** Book management view */
  @GetMapping("/books")
  public String manageBooks(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String search,
      Model model) {

    // Delegate to book controller but with admin view
    model.addAttribute("adminView", true);

    return "forward:/books";
  }

  /** User management view */
  @GetMapping("/users")
  public String manageUsers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String search,
      Model model) {

    List<LibraryUser> users;

    if (search != null && !search.isEmpty()) {
      users = userService.searchUsers(search);
    } else {
      users = userService.getAllUsers(page, size);
    }

    model.addAttribute("users", users);

    return "admin/users";
  }

  /** User detail view with HTMX */
  @GetMapping("/users/{id}")
  public String viewUser(@PathVariable UUID id, Model model) {
    LibraryUser user = userService.getUserById(id);
    List<BookLoan> userLoans = loanService.getLoansForUser(user);

    model.addAttribute("user", user);
    model.addAttribute("loans", userLoans);

    return "admin/fragments :: user-details";
  }

  /** Toggle user admin status with HTMX */
  @PostMapping("/users/{id}/toggle-admin")
  public String toggleAdminStatus(@PathVariable UUID id, Model model) {
    LibraryUser user = userService.toggleAdminStatus(id);
    model.addAttribute("user", user);

    return "admin/fragments :: user-row";
  }

  /** Notifications management */
  @GetMapping("/notifications")
  public String manageNotifications(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String type,
      Model model) {

    List<Notification> notifications;

    if ((status != null && !status.isEmpty()) || (type != null && !type.isEmpty())) {
      notifications = notificationService.filterNotifications(status, type, page, size);
    } else {
      notifications = notificationService.getAllNotifications(page, size);
    }

    model.addAttribute("notifications", notifications);
    model.addAttribute("statuses", Notification.NotificationStatus.values());
    model.addAttribute("types", Notification.NotificationType.values());

    return "admin/notifications";
  }

  /** Resend failed notification with HTMX */
  @PostMapping("/notifications/{id}/resend")
  public String resendNotification(@PathVariable UUID id, Model model) {
    try {
      Notification notification = notificationService.resendNotification(id);
      model.addAttribute("notification", notification);
      return "admin/fragments :: notification-row";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "admin/fragments :: notification-error";
    }
  }

  /** System health check with HTMX */
  @GetMapping("/system-health")
  public String systemHealth(Model model) {
    Map<String, Object> healthData = statsService.getSystemHealth();
    model.addAttribute("health", healthData);

    return "admin/fragments :: system-health";
  }

  /** Trigger notification job manually */
  @PostMapping("/run-notification-job")
  public String runNotificationJob(@RequestParam String jobType, Model model) {
    try {
      String result = statsService.triggerJob(jobType);
      model.addAttribute("jobResult", result);
      return "admin/fragments :: job-result";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "admin/fragments :: job-error";
    }
  }

  /** Library locations management */
  @GetMapping("/locations")
  public String manageLocations(Model model) {
    model.addAttribute("locations", locationService.getAllLocations());
    return "admin/locations";
  }

  /** Add/edit location with HTMX */
  @GetMapping("/locations/form")
  public String locationForm(@RequestParam(required = false) UUID id, Model model) {
    if (id != null) {
      model.addAttribute("location", locationService.getLocationById(id));
      model.addAttribute("editing", true);
    } else {
      model.addAttribute("location", new LibraryLocation());
      model.addAttribute("editing", false);
    }

    return "admin/fragments :: location-form";
  }

  /** Save location with HTMX */
  @PostMapping("/locations/save")
  public String saveLocation(LibraryLocation location, Model model) {
    try {
      LibraryLocation savedLocation = locationService.saveLocation(location);

      List<LibraryLocation> allLocations = locationService.getAllLocations();
      model.addAttribute("locations", allLocations);
      model.addAttribute("success", true);

      return "admin/fragments :: locations-table";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      model.addAttribute("location", location);
      return "admin/fragments :: location-form";
    }
  }

  /** Delete location with HTMX */
  @DeleteMapping("/locations/{id}")
  public String deleteLocation(@PathVariable UUID id, Model model) {
    try {
      locationService.deleteLocation(id);

      List<LibraryLocation> allLocations = locationService.getAllLocations();
      model.addAttribute("locations", allLocations);

      return "admin/fragments :: locations-table";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "admin/fragments :: location-error";
    }
  }
}
