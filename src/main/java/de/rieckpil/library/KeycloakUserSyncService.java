package de.rieckpil.library;

import java.util.Map;
import java.util.Optional;

import de.rieckpil.library.model.LibraryUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KeycloakUserSyncService {

  private final LibraryUserRepository libraryUserRepository;

  public KeycloakUserSyncService(LibraryUserRepository libraryUserRepository) {
    this.libraryUserRepository = libraryUserRepository;
  }

  @Transactional
  public LibraryUser syncUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (!(authentication instanceof OAuth2AuthenticationToken)) {
      throw new IllegalStateException("User is not authenticated with OAuth2/Keycloak");
    }

    OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
    Map<String, Object> attributes = oauth2Token.getPrincipal().getAttributes();

    // The sub claim is the Keycloak ID
    String keycloakId = (String) attributes.get("sub");

    Optional<LibraryUser> existingUser = libraryUserRepository.findByKeycloakId(keycloakId);

    return existingUser
      .map(libraryUser -> updateExistingUser(libraryUser, attributes))
      .orElseGet(() -> createNewUser(keycloakId, attributes));
  }

  private LibraryUser updateExistingUser(LibraryUser user, Map<String, Object> attributes) {
    updateUserAttributes(user, attributes);
    return libraryUserRepository.save(user);
  }

  private LibraryUser createNewUser(String keycloakId, Map<String, Object> attributes) {
    LibraryUser newUser = new LibraryUser();
    newUser.setKeycloakId(keycloakId);
    updateUserAttributes(newUser, attributes);
    return libraryUserRepository.save(newUser);
  }

  private void updateUserAttributes(LibraryUser user, Map<String, Object> attributes) {
    user.setFirstName(getAttributeAsString(attributes, "given_name"));
    user.setLastName(getAttributeAsString(attributes, "family_name"));
    user.setEmail(getAttributeAsString(attributes, "email"));

    // Check for admin role in the authorities
    boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
      .getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    user.setAdmin(isAdmin);
  }

  private String getAttributeAsString(Map<String, Object> attributes, String attributeName) {
    Object value = attributes.get(attributeName);
    return value != null ? value.toString() : "";
  }
}
