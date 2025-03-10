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
import org.springframework.stereotype.Service;

@Service
public class BookReviewService {
  List<BookReview> getReviewsByBook(Book book) {
    return null;
  }

  Double getAverageRatingForBook(Book book) {
    return null;
  }

  Map<Integer, Long> getRatingDistributionForBook(Book book) {
    return null;
  }

  boolean hasUserReviewedBook(Book book, LibraryUser currentUser) {
    return false;
  }

  BookReview getReviewByBookAndUser(Book book, LibraryUser currentUser) {
    return null;
  }

  BookReview updateReview(
      Book book,
      LibraryUser currentUser,
      @Min(1) @Max(5) Integer rating,
      @Size(max = 1000) String comment) {
    return null;
  }

  BookReview createReview(
      Book book,
      LibraryUser currentUser,
      @Min(1) @Max(5) Integer rating,
      @Size(max = 1000) String comment) {
    return null;
  }

  BookReview getReviewById(UUID reviewId) {
    return null;
  }

  void deleteReview(UUID reviewId) {}

  void voteOnReview(UUID reviewId, LibraryUser currentUser, boolean helpful) {}

  List<BookReview> getRecentReviews(int limit) {
    return null;
  }

  BookReview flagReview(UUID reviewId) {
    return null;
  }
}
