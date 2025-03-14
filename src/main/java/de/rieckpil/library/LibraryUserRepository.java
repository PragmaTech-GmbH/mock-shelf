package de.rieckpil.library;

import java.util.Optional;
import java.util.UUID;

import de.rieckpil.library.model.LibraryUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryUserRepository extends JpaRepository<LibraryUser, UUID> {
  Optional<LibraryUser> findByKeycloakId(String keycloakId);

  Optional<LibraryUser> findByEmail(String email);
}
