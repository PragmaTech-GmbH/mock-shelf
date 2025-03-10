package de.rieckpil.library;

import java.util.List;
import java.util.UUID;

import de.rieckpil.library.model.LibraryLocation;
import org.springframework.stereotype.Service;

@Service
public class LibraryLocationService {
  List<LibraryLocation> getAllLocations() {
    return null;
  }

  LibraryLocation getLocationById(UUID id) {
    return null;
  }

  LibraryLocation saveLocation(LibraryLocation location) {
    return null;
  }

  void deleteLocation(UUID id) {}
}
