package de.rieckpil.library;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.rieckpil.library.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

  private final BookRepository bookRepository;
  private final IsbnLookupService isbnLookupService;

  public BookService(BookRepository bookRepository, IsbnLookupService isbnLookupService) {
    this.bookRepository = bookRepository;
    this.isbnLookupService = isbnLookupService;
  }

  /**
   * Get a book by ID
   *
   * @param id Book ID
   * @return Book if found
   * @throws BookNotFoundException if book not found
   */
  public Book getBookById(UUID id) {
    return bookRepository
        .findById(id)
        .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
  }

  /**
   * Get a book by ISBN
   *
   * @param isbn ISBN
   * @return Book if found
   * @throws BookNotFoundException if book not found
   */
  public Book getBookByIsbn(String isbn) {
    return bookRepository
        .findByIsbn(isbn)
        .orElseThrow(() -> new BookNotFoundException("Book not found with ISBN: " + isbn));
  }

  /**
   * Search for books by title, author, ISBN, or category
   *
   * @param searchTerm Search term
   * @param pageable Pagination information
   * @return Page of books
   */
  public Page<Book> searchBooks(String searchTerm, Pageable pageable) {
    if (searchTerm == null || searchTerm.isBlank()) {
      return bookRepository.findAll(pageable);
    }
    return bookRepository.search(searchTerm, pageable);
  }

  /**
   * Get books by availability
   *
   * @param available Availability flag
   * @return List of books
   */
  public List<Book> getBooksByAvailability(boolean available) {
    return bookRepository.findByAvailable(available);
  }

  /**
   * Get all books with pagination
   *
   * @param pageable Pagination information
   * @return Page of books
   */
  public Page<Book> getAllBooks(Pageable pageable) {
    return bookRepository.findAll(pageable);
  }

  /**
   * Get most borrowed books
   *
   * @param limit Maximum number of books to return
   * @return List of most borrowed books
   */
  public List<Book> getMostBorrowedBooks(int limit) {
    return bookRepository.findMostBorrowedBooks(limit);
  }

  /**
   * Create a new book
   *
   * @param book Book to create
   * @return Created book
   * @throws BookAlreadyExistsException if book with same ISBN already exists
   */
  @Transactional
  public Book createBook(Book book) {
    // Check if book with same ISBN already exists
    Optional<Book> existingBook = bookRepository.findByIsbn(book.getIsbn());
    if (existingBook.isPresent()) {
      throw new BookAlreadyExistsException("Book already exists with ISBN: " + book.getIsbn());
    }

    return bookRepository.save(book);
  }

  /**
   * Update an existing book
   *
   * @param id Book ID
   * @param bookDetails Updated book details
   * @return Updated book
   * @throws BookNotFoundException if book not found
   */
  @Transactional
  public Book updateBook(UUID id, Book bookDetails) {
    Book book = getBookById(id);

    // If ISBN is being changed, check it doesn't conflict with another book
    if (!book.getIsbn().equals(bookDetails.getIsbn())) {
      bookRepository
          .findByIsbn(bookDetails.getIsbn())
          .ifPresent(
              existingBook -> {
                if (!existingBook.getId().equals(id)) {
                  throw new BookAlreadyExistsException(
                      "Book already exists with ISBN: " + bookDetails.getIsbn());
                }
              });
    }

    // Update fields
    book.setIsbn(bookDetails.getIsbn());
    book.setTitle(bookDetails.getTitle());
    book.setAuthor(bookDetails.getAuthor());
    book.setPublisher(bookDetails.getPublisher());
    book.setPublicationDate(bookDetails.getPublicationDate());
    book.setDescription(bookDetails.getDescription());
    book.setCoverImageUrl(bookDetails.getCoverImageUrl());
    book.setPageCount(bookDetails.getPageCount());
    book.setLanguage(bookDetails.getLanguage());
    book.setCategory(bookDetails.getCategory());
    book.setAvailable(bookDetails.getAvailable());

    return bookRepository.save(book);
  }

  /**
   * Delete a book
   *
   * @param id Book ID
   * @throws BookNotFoundException if book not found
   */
  @Transactional
  public void deleteBook(UUID id) {
    if (!bookRepository.existsById(id)) {
      throw new BookNotFoundException("Book not found with ID: " + id);
    }
    bookRepository.deleteById(id);
  }

  /**
   * Update book availability
   *
   * @param id Book ID
   * @param available Availability flag
   * @return Updated book
   * @throws BookNotFoundException if book not found
   */
  @Transactional
  public Book updateAvailability(UUID id, boolean available) {
    Book book = getBookById(id);
    book.setAvailable(available);
    return bookRepository.save(book);
  }

  /**
   * Lookup book information by ISBN from external API and create a book
   *
   * @param isbn ISBN to lookup
   * @return The newly created book
   */
  @Transactional
  public Optional<Book> lookupAndCreateBook(String isbn) {
    // Check if book with ISBN already exists
    Optional<Book> existingBook = bookRepository.findByIsbn(isbn);
    if (existingBook.isPresent()) {
      throw new BookAlreadyExistsException("Book already exists with ISBN: " + isbn);
    }

    // Lookup book by ISBN
    return isbnLookupService
        .lookupByIsbn(isbn)
        .map(isbnLookupService::convertToBookEntity)
        .map(bookRepository::save);
  }
}
