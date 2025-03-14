package de.rieckpil.library;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.rieckpil.library.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

  public Book getBookById(UUID id) {
    return bookRepository
        .findById(id)
        .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
  }

  public Book getBookByIsbn(String isbn) {
    // Normalize ISBN by removing hyphens and spaces
    String normalizedIsbn = normalizeIsbn(isbn);
    return bookRepository
        .findByIsbn(normalizedIsbn)
        .orElseThrow(() -> new BookNotFoundException("Book not found with ISBN: " + isbn));
  }

  public Page<Book> searchBooks(String searchTerm, Pageable pageable) {
    if (searchTerm == null || searchTerm.isBlank()) {
      return bookRepository.findAll(pageable);
    }
    return bookRepository.search(searchTerm, pageable);
  }

  public List<Book> getBooksByAvailability(boolean available) {
    return bookRepository.findByAvailable(available);
  }

  public Page<Book> getAllBooks(Pageable pageable) {
    return bookRepository.findAll(pageable);
  }

  public List<Book> getMostBorrowedBooks(int limit) {
    return bookRepository.findMostBorrowedBooks(limit);
  }

  @Transactional
  public Book createBook(Book book) {
    // Normalize ISBN
    book.setIsbn(normalizeIsbn(book.getIsbn()));

    // Check if book with same ISBN already exists
    Optional<Book> existingBook = bookRepository.findByIsbn(book.getIsbn());
    if (existingBook.isPresent()) {
      throw new BookAlreadyExistsException("Book already exists with ISBN: " + book.getIsbn());
    }

    return bookRepository.save(book);
  }

  @Transactional
  public Book updateBook(UUID id, Book bookDetails) {
    Book book = getBookById(id);

    // Normalize ISBN
    bookDetails.setIsbn(normalizeIsbn(bookDetails.getIsbn()));

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

  @Transactional
  public void deleteBook(UUID id) {
    if (!bookRepository.existsById(id)) {
      throw new BookNotFoundException("Book not found with ID: " + id);
    }
    bookRepository.deleteById(id);
  }

  @Transactional
  public Book updateAvailability(UUID id, boolean available) {
    Book book = getBookById(id);
    book.setAvailable(available);
    return bookRepository.save(book);
  }

  @Transactional
  public Optional<Book> lookupAndCreateBook(String isbn) {
    // Normalize ISBN
    String normalizedIsbn = normalizeIsbn(isbn);

    // Check if book with ISBN already exists
    Optional<Book> existingBook = bookRepository.findByIsbn(normalizedIsbn);
    if (existingBook.isPresent()) {
      return existingBook;
    }

    // Lookup book by ISBN
    try {
      return isbnLookupService
          .lookupByIsbn(normalizedIsbn)
          .map(
              bookData -> {
                Book book = isbnLookupService.convertToBookEntity(bookData);
                return bookRepository.save(book);
              });
    } catch (Exception e) {
      throw new IsbnLookupException("Failed to lookup ISBN: " + e.getMessage());
    }
  }

  public Long countAllBooks() {
    return bookRepository.count();
  }

  public Long countAvailableBooks() {
    return Long.valueOf(bookRepository.findByAvailable(true).size());
  }

  public List<Book> getRecentlyAddedBooks(int amount) {
    return bookRepository.findRecentlyAddedBooks(PageRequest.of(0, amount));
  }

  // Helper method to normalize ISBN by removing hyphens and spaces
  private String normalizeIsbn(String isbn) {
    if (isbn == null) {
      return null;
    }
    return isbn.replaceAll("[-\\s]", "").trim();
  }
}
