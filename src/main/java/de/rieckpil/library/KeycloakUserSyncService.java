package de.rieckpil.library;

import java.util.Optional;

import de.rieckpil.library.model.LibraryUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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

    if (!(authentication instanceof JwtAuthenticationToken)) {
      throw new IllegalStateException("User is not authenticated with OAuth2/Keycloak");
    }

    JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
    String keycloakId = jwtToken.getName();

    Optional<LibraryUser> existingUser = libraryUserRepository.findByKeycloakId(keycloakId);

    return existingUser
        .map(libraryUser -> updateExistingUser(libraryUser, jwtToken))
        .orElseGet(() -> createNewUser(jwtToken));
  }

  private LibraryUser updateExistingUser(LibraryUser user, JwtAuthenticationToken jwtToken) {
    updateUserAttributes(user, jwtToken);
    return libraryUserRepository.save(user);
  }

  private LibraryUser createNewUser(JwtAuthenticationToken jwtToken) {
    LibraryUser newUser = new LibraryUser();
    newUser.setKeycloakId(jwtToken.getName());
    updateUserAttributes(newUser, jwtToken);
    return libraryUserRepository.save(newUser);
  }

  private void updateUserAttributes(LibraryUser user, JwtAuthenticationToken jwtToken) {
    user.setFirstName(getClaimAsString(jwtToken, "given_name"));
    user.setLastName(getClaimAsString(jwtToken, "family_name"));
    user.setEmail(getClaimAsString(jwtToken, "email"));

    // Check for admin role in the token
    boolean isAdmin =
        jwtToken.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    user.setAdmin(isAdmin);
  }

  private String getClaimAsString(JwtAuthenticationToken token, String claimName) {
    Object claim = token.getToken().getClaims().get(claimName);
    return claim != null ? claim.toString() : "";
  }
}
