package de.rieckpil.library;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.rieckpil.library.model.Book;
import de.rieckpil.library.model.BookReview;
import de.rieckpil.library.model.LibraryUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
@RequestMapping("/reviews")
public class ReviewController {

  private final BookReviewService reviewService;
  private final BookService bookService;
  private final UserService userService;

  public ReviewController(
      BookReviewService reviewService, BookService bookService, UserService userService) {
    this.reviewService = reviewService;
    this.bookService = bookService;
    this.userService = userService;
  }

  /** Get reviews for a book with HTMX */
  @GetMapping("/book/{bookId}")
  public String getBookReviews(@PathVariable UUID bookId, Model model) {
    Book book = bookService.getBookById(bookId);
    List<BookReview> reviews = reviewService.getReviewsByBook(book);

    Double averageRating = reviewService.getAverageRatingForBook(book);
    Map<Integer, Long> ratingDistribution = reviewService.getRatingDistributionForBook(book);

    // Check if current user has already reviewed
    boolean userHasReviewed = false;
    if (userService.isAuthenticated()) {
      LibraryUser currentUser = userService.getCurrentUser();
      if (currentUser != null) {
        userHasReviewed = reviewService.hasUserReviewedBook(book, currentUser);
      }
    }

    model.addAttribute("book", book);
    model.addAttribute("reviews", reviews);
    model.addAttribute("averageRating", averageRating);
    model.addAttribute("ratingDistribution", ratingDistribution);
    model.addAttribute("userHasReviewed", userHasReviewed);

    return "reviews/book-reviews";
  }

  /** Show review form with HTMX */
  @GetMapping("/create/{bookId}")
  @PreAuthorize("isAuthenticated()")
  public String showReviewForm(@PathVariable UUID bookId, Model model) {
    Book book = bookService.getBookById(bookId);
    LibraryUser currentUser = userService.getCurrentUser();

    // Check if user has already reviewed this book
    if (reviewService.hasUserReviewedBook(book, currentUser)) {
      BookReview existingReview = reviewService.getReviewByBookAndUser(book, currentUser);
      model.addAttribute("review", existingReview);
      model.addAttribute("editing", true);
    } else {
      model.addAttribute("review", new BookReview());
      model.addAttribute("editing", false);
    }

    model.addAttribute("book", book);

    return "reviews/fragments :: review-form";
  }

  /** Submit a review with HTMX */
  @PostMapping("/submit")
  @PreAuthorize("isAuthenticated()")
  public String submitReview(
      @RequestParam UUID bookId,
      @RequestParam @Min(1) @Max(5) Integer rating,
      @RequestParam @Size(max = 1000) String comment,
      Model model) {

    try {
      Book book = bookService.getBookById(bookId);
      LibraryUser currentUser = userService.getCurrentUser();

      // Check if updating existing review
      BookReview review;
      if (reviewService.hasUserReviewedBook(book, currentUser)) {
        review = reviewService.updateReview(book, currentUser, rating, comment);
      } else {
        review = reviewService.createReview(book, currentUser, rating, comment);
      }

      // Refresh reviews for the book
      List<BookReview> reviews = reviewService.getReviewsByBook(book);
      Double averageRating = reviewService.getAverageRatingForBook(book);

      model.addAttribute("book", book);
      model.addAttribute("reviews", reviews);
      model.addAttribute("averageRating", averageRating);
      model.addAttribute("newReview", review);

      return "reviews/fragments :: reviews-section";

    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "reviews/fragments :: review-error";
    }
  }

  /** Delete a review with HTMX */
  @DeleteMapping("/{reviewId}")
  @PreAuthorize("isAuthenticated()")
  public String deleteReview(@PathVariable UUID reviewId, Model model) {
    try {
      BookReview review = reviewService.getReviewById(reviewId);

      // Security check - ensure user can only delete their own reviews
      LibraryUser currentUser = userService.getCurrentUser();

      if (!review.getUser().getId().equals(currentUser.getId()) && !userService.isAdmin()) {
        throw new SecurityException("You can only delete your own reviews");
      }

      reviewService.deleteReview(reviewId);

      // Refresh reviews for the book
      Book book = review.getBook();
      List<BookReview> reviews = reviewService.getReviewsByBook(book);
      Double averageRating = reviewService.getAverageRatingForBook(book);

      model.addAttribute("book", book);
      model.addAttribute("reviews", reviews);
      model.addAttribute("averageRating", averageRating);

      return "reviews/fragments :: reviews-section";

    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "reviews/fragments :: review-error";
    }
  }

  /** Upvote/downvote a review (for helpful marking) */
  @PostMapping("/{reviewId}/vote")
  @PreAuthorize("isAuthenticated()")
  public String voteReview(
      @PathVariable UUID reviewId, @RequestParam boolean helpful, Model model) {

    try {
      LibraryUser currentUser = userService.getCurrentUser();

      reviewService.voteOnReview(reviewId, currentUser, helpful);

      // Get updated review
      BookReview review = reviewService.getReviewById(reviewId);
      model.addAttribute("review", review);

      return "reviews/fragments :: review-votes";

    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "reviews/fragments :: vote-error";
    }
  }

  /** Get recent reviews for homepage/dashboard with HTMX */
  @GetMapping("/recent")
  public String getRecentReviews(@RequestParam(defaultValue = "5") int limit, Model model) {

    List<BookReview> recentReviews = reviewService.getRecentReviews(limit);
    model.addAttribute("reviews", recentReviews);

    return "reviews/fragments :: recent-reviews";
  }

  /** Admin: Flag a review as inappropriate */
  @PostMapping("/{reviewId}/flag")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public String flagReview(@PathVariable UUID reviewId, Model model) {
    try {
      BookReview review = reviewService.flagReview(reviewId);
      model.addAttribute("review", review);
      return "reviews/fragments :: review-item";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "reviews/fragments :: flag-error";
    }
  }
}
