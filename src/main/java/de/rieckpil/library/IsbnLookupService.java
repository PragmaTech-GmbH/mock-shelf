package de.rieckpil.library;

import java.util.Optional;

import de.rieckpil.library.model.Book;
import org.springframework.stereotype.Service;

@Service
public class IsbnLookupService {
  Optional<Object> lookupByIsbn(String isbn) {
    return null;
  }

  Book convertToBookEntity(Object o) {
    return null;
  }
}
