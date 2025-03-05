package de.rieckpil.library;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.rieckpil.library.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, UUID> {

  /** Find a book by its ISBN */
  Optional<Book> findByIsbn(String isbn);

  /** Find books by title (case-insensitive, partial match) */
  List<Book> findByTitleContainingIgnoreCase(String title);

  /** Find books by author (case-insensitive, partial match) */
  List<Book> findByAuthorContainingIgnoreCase(String author);

  /** Find books by category (case-insensitive, partial match) */
  List<Book> findByCategoryContainingIgnoreCase(String category);

  /** Find books by availability status */
  List<Book> findByAvailable(Boolean available);

  /** Search for books by multiple fields (title, author, ISBN, publisher, category) */
  @Query(
      "SELECT b FROM Book b WHERE "
          + "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(b.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<Book> search(@Param("searchTerm") String searchTerm, Pageable pageable);

  /** Find books published between two dates */
  @Query("SELECT b FROM Book b WHERE " + "b.publicationDate BETWEEN :startDate AND :endDate")
  List<Book> findByPublicationDateBetween(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  /** Find the most borrowed books based on loan count */
  @Query(
      value =
          "SELECT b.* FROM books b "
              + "JOIN book_loans l ON b.id = l.book_id "
              + "GROUP BY b.id "
              + "ORDER BY COUNT(l.id) DESC "
              + "LIMIT :limit",
      nativeQuery = true)
  List<Book> findMostBorrowedBooks(@Param("limit") int limit);

  /** Find recently added books */
  @Query("SELECT b FROM Book b ORDER BY b.createdAt DESC")
  List<Book> findRecentlyAddedBooks(Pageable pageable);

  /** Count books by category */
  @Query("SELECT b.category, COUNT(b) FROM Book b GROUP BY b.category ORDER BY COUNT(b) DESC")
  List<Object[]> countBooksByCategory();

  /** Full-text search for books (if database supports it) */
  @Query(
      value =
          "SELECT * FROM books b WHERE "
              + "to_tsvector('english', b.title || ' ' || b.author || ' ' || COALESCE(b.description, '')) "
              + "@@ plainto_tsquery('english', :query)",
      nativeQuery = true)
  List<Book> fullTextSearch(@Param("query") String query, Pageable pageable);
}
