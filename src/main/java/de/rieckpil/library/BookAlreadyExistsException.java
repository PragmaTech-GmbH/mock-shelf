package de.rieckpil.library;

public class BookAlreadyExistsException extends RuntimeException {

  BookAlreadyExistsException(String message) {
    super(message);
  }
}
