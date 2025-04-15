package de.rieckpil.library;

import java.util.List;
import java.util.UUID;

import de.rieckpil.library.model.LibraryUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final KeycloakUserSyncService keycloakUserSyncService;
  private final LibraryUserRepository libraryUserRepository;

  public UserService(
      KeycloakUserSyncService keycloakUserSyncService,
      LibraryUserRepository libraryUserRepository) {
    this.keycloakUserSyncService = keycloakUserSyncService;
    this.libraryUserRepository = libraryUserRepository;
  }

  Long countAllUsers() {
    return null;
  }

  List<LibraryUser> getRecentlyRegisteredUsers(int amount) {
    return null;
  }

  List<LibraryUser> searchUsers(String search) {
    return null;
  }

  List<LibraryUser> getAllUsers(int page, int size) {
    return null;
  }

  LibraryUser getUserById(UUID id) {
    return null;
  }

  LibraryUser toggleAdminStatus(UUID id) {
    return null;
  }

  boolean isAuthenticated() {
    return false;
  }

  public LibraryUser getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return null;
    }

    return keycloakUserSyncService.syncUser();
  }

  boolean isAdmin() {
    return false;
  }

  @Transactional
  public LibraryUser updateUser(LibraryUser user) {
    return libraryUserRepository.save(user);
  }
}
