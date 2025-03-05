package de.rieckpil.library;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.rieckpil.library.model.Book;
import de.rieckpil.library.model.BookReview;
import de.rieckpil.library.model.LibraryUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookReviewRepository extends JpaRepository<BookReview, UUID> {

  /** Find all reviews for a specific book */
  List<BookReview> findByBook(Book book);

  /** Find all reviews by a specific user */
  List<BookReview> findByUser(LibraryUser user);

  /** Find a specific user's review of a specific book */
  Optional<BookReview> findByBookAndUser(Book book, LibraryUser user);

  /** Calculate the average rating for a book */
  @Query("SELECT AVG(r.rating) FROM BookReview r WHERE r.book = :book")
  Double getAverageRatingForBook(@Param("book") Book book);

  /** Count the number of reviews for a book */
  @Query("SELECT COUNT(r) FROM BookReview r WHERE r.book = :book")
  Long countReviewsForBook(@Param("book") Book book);

  /** Find the top rated books */
  @Query(
      "SELECT r.book.id, AVG(r.rating) as avgRating "
          + "FROM BookReview r "
          + "GROUP BY r.book.id "
          + "ORDER BY avgRating DESC")
  List<Object[]> findTopRatedBooks(Pageable pageable);

  /** Find recent reviews */
  @Query("SELECT r FROM BookReview r ORDER BY r.createdAt DESC")
  List<BookReview> findRecentReviews(Pageable pageable);

  /** Find reviews with a specific rating */
  List<BookReview> findByRating(Integer rating);

  /** Get distribution of ratings for a book (1-5 stars) */
  @Query(
      "SELECT r.rating, COUNT(r) "
          + "FROM BookReview r "
          + "WHERE r.book = :book "
          + "GROUP BY r.rating "
          + "ORDER BY r.rating")
  List<Object[]> getRatingDistributionForBook(@Param("book") Book book);

  /** Find reviews containing specific text in the comment */
  @Query(
      "SELECT r FROM BookReview r WHERE LOWER(r.comment) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  List<BookReview> searchReviewComments(@Param("searchTerm") String searchTerm, Pageable pageable);

  /** Check if a user has already reviewed a book */
  @Query("SELECT COUNT(r) > 0 FROM BookReview r WHERE r.book = :book AND r.user = :user")
  boolean hasUserReviewedBook(@Param("book") Book book, @Param("user") LibraryUser user);
}
