package de.rieckpil.library;

import java.util.List;
import java.util.UUID;

import de.rieckpil.library.model.LibraryLocation;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for library locations. */
public interface LibraryLocationRepository extends JpaRepository<LibraryLocation, UUID> {

  /**
   * Find library locations by name (case-insensitive).
   *
   * @param name the name to search for
   * @return list of matching locations
   */
  List<LibraryLocation> findByNameContainingIgnoreCase(String name);

  /**
   * Find library locations by address (case-insensitive).
   *
   * @param address the address fragment to search for
   * @return list of matching locations
   */
  List<LibraryLocation> findByAddressContainingIgnoreCase(String address);
}
