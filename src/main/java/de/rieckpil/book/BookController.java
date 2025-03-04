package de.rieckpil.book;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.mockshelf.dto.BookCreateRequest;
import com.mockshelf.exception.BookAlreadyExistsException;
import com.mockshelf.exception.BookNotFoundException;
import com.mockshelf.exception.IsbnLookupException;
import com.mockshelf.model.Book;
import com.mockshelf.repository.BookLoanRepository;
import com.mockshelf.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

  private final BookService bookService;
  private final BookLoanRepository bookLoanRepository;

  @GetMapping
  public String listBooks(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String search,
      Model model) {

    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("title").ascending());
    Page<Book> bookPage;

    if (search != null && !search.isBlank()) {
      bookPage = bookService.searchBooks(search, pageRequest);
      model.addAttribute("search", search);
    } else {
      bookPage = bookService.getAllBooks(pageRequest);
    }

    model.addAttribute("bookPage", bookPage);

    if (bookPage.getTotalPages() > 0) {
      List<Integer> pageNumbers =
          IntStream.rangeClosed(0, bookPage.getTotalPages() - 1)
              .boxed()
              .collect(Collectors.toList());
      model.addAttribute("pageNumbers", pageNumbers);
    }

    return "books/list";
  }

  @GetMapping("/{id}")
  public String viewBook(@PathVariable UUID id, Model model) {
    try {
      Book book = bookService.getBookById(id);
      model.addAttribute("book", book);

      // Check if book is on loan
      bookLoanRepository
          .findActiveBookLoan(book)
          .ifPresent(
              loan -> {
                model.addAttribute("activeLoan", loan);
              });

      return "books/view";
    } catch (BookNotFoundException e) {
      return "redirect:/books";
    }
  }

  @GetMapping("/new")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public String showCreateForm(Model model) {
    model.addAttribute("book", new Book());
    model.addAttribute("isNew", true);
    return "books/form";
  }

  @PostMapping("/new")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public String createBook(
      @Valid @ModelAttribute("book") Book book,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {

    if (result.hasErrors()) {
      model.addAttribute("isNew", true);
      return "books/form";
    }

    try {
      Book createdBook = bookService.createBook(book);
      redirectAttributes.addFlashAttribute("message", "Book created successfully!");
      return "redirect:/books/" + createdBook.getId();
    } catch (BookAlreadyExistsException e) {
      result.rejectValue("isbn", "error.book", e.getMessage());
      model.addAttribute("isNew", true);
      return "books/form";
    }
  }

  @GetMapping("/{id}/edit")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public String showEditForm(@PathVariable UUID id, Model model) {
    try {
      Book book = bookService.getBookById(id);
      model.addAttribute("book", book);
      model.addAttribute("isNew", false);
      return "books/form";
    } catch (BookNotFoundException e) {
      return "redirect:/books";
    }
  }

  @PostMapping("/{id}/edit")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public String updateBook(
      @PathVariable UUID id,
      @Valid @ModelAttribute("book") Book book,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {

    if (result.hasErrors()) {
      model.addAttribute("isNew", false);
      return "books/form";
    }

    try {
      Book updatedBook = bookService.updateBook(id, book);
      redirectAttributes.addFlashAttribute("message", "Book updated successfully!");
      return "redirect:/books/" + updatedBook.getId();
    } catch (BookNotFoundException e) {
      return "redirect:/books";
    } catch (BookAlreadyExistsException e) {
      result.rejectValue("isbn", "error.book", e.getMessage());
      model.addAttribute("isNew", false);
      return "books/form";
    }
  }

  @GetMapping("/{id}/delete")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public String deleteBook(@PathVariable UUID id, RedirectAttributes redirectAttributes) {

    try {
      Book book = bookService.getBookById(id);
      bookService.deleteBook(id);
      redirectAttributes.addFlashAttribute(
          "message", "Book '" + book.getTitle() + "' deleted successfully!");
    } catch (BookNotFoundException e) {
      // Book already deleted or not found
    }

    return "redirect:/books";
  }

  @GetMapping("/isbn-lookup")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public String showIsbnLookupForm(Model model) {
    model.addAttribute("isbnLookup", new BookCreateRequest());
    return "books/isbn-lookup";
  }

  @PostMapping("/isbn-lookup")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public String processIsbnLookup(
      @Valid @ModelAttribute("isbnLookup") BookCreateRequest request,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {

    if (result.hasErrors()) {
      return "books/isbn-lookup";
    }

    try {
      // Check if book already exists
      try {
        Book existingBook = bookService.getBookByIsbn(request.getIsbn());
        redirectAttributes.addFlashAttribute(
            "warning", "Book with ISBN " + request.getIsbn() + " already exists.");
        return "redirect:/books/" + existingBook.getId() + "/edit";
      } catch (BookNotFoundException e) {
        // Book doesn't exist, proceed with lookup
      }

      // Do the lookup and create the book
      Book book = bookService.lookupAndCreateBook(request.getIsbn()).block();

      redirectAttributes.addFlashAttribute(
          "message", "Book information fetched and saved successfully!");

      return "redirect:/books/" + book.getId();
    } catch (IsbnLookupException e) {
      model.addAttribute("error", "Error looking up ISBN: " + e.getMessage());
      return "books/isbn-lookup";
    } catch (BookAlreadyExistsException e) {
      model.addAttribute("error", e.getMessage());
      return "books/isbn-lookup";
    } catch (Exception e) {
      log.error("Unexpected error during ISBN lookup: {}", e.getMessage(), e);
      model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
      return "books/isbn-lookup";
    }
  }
}
