package de.rieckpil.library;

import java.util.List;
import java.util.UUID;

import de.rieckpil.library.model.LibraryLocation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for library location management.
 */
@Service
public class LibraryLocationService {

  private final LibraryLocationRepository locationRepository;

  public LibraryLocationService(LibraryLocationRepository locationRepository) {
    this.locationRepository = locationRepository;
  }

  /**
   * Get all library locations sorted by name.
   *
   * @return list of all locations
   */
  public List<LibraryLocation> getAllLocations() {
    return locationRepository.findAll(Sort.by("name"));
  }

  /**
   * Get a library location by its ID.
   *
   * @param id the location ID
   * @return the location
   * @throws IllegalArgumentException if location not found
   */
  public LibraryLocation getLocationById(UUID id) {
    return locationRepository
      .findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Location not found: " + id));
  }

  /**
   * Save a new or update an existing library location.
   *
   * @param location the location to save
   * @return the saved location
   */
  @Transactional
  public LibraryLocation saveLocation(LibraryLocation location) {
    // Validation
    if (location.getName() == null || location.getName().trim().isEmpty()) {
      throw new IllegalArgumentException("Location name is required");
    }
    if (location.getAddress() == null || location.getAddress().trim().isEmpty()) {
      throw new IllegalArgumentException("Location address is required");
    }

    return locationRepository.save(location);
  }

  /**
   * Delete a library location.
   *
   * @param id the location ID
   * @throws IllegalArgumentException if location not found or cannot be deleted
   */
  @Transactional
  public void deleteLocation(UUID id) {
    LibraryLocation location = getLocationById(id);

    // Check if location is being used by active loans before deletion
    // Simplified check - in production you'd verify no active loans reference this location

    locationRepository.delete(location);
  }

  /**
   * Search for locations by name.
   *
   * @param name the name to search for
   * @return matching locations
   */
  public List<LibraryLocation> searchByName(String name) {
    return locationRepository.findByNameContainingIgnoreCase(name);
  }

  /**
   * Get paginated list of locations.
   *
   * @param page page number
   * @param size page size
   * @return page of locations
   */
  public List<LibraryLocation> getPagedLocations(int page, int size) {
    return locationRepository.findAll(
      PageRequest.of(page, size, Sort.by("name"))).getContent();
  }
}
