package de.rieckpil.library;

import java.util.List;

import de.rieckpil.library.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

  private final BookService bookService;

  @Autowired
  public HomeController(BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping("/")
  public String index(Model model) {
    List<Book> recentBooks = bookService.getRecentlyAddedBooks(4);
    model.addAttribute("recentBooks", recentBooks);

    return "index";
  }
}
