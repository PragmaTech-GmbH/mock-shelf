package de.rieckpil.library.configuration;

import java.util.List;

import de.rieckpil.library.CurrentUser;
import de.rieckpil.library.KeycloakUserSyncService;
import de.rieckpil.library.LibraryUserRepository;
import de.rieckpil.library.model.LibraryUser;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

  private final KeycloakUserSyncService keycloakUserSyncService;
  private final LibraryUserRepository libraryUserRepository;

  public WebMvcConfiguration(
      KeycloakUserSyncService keycloakUserSyncService,
      LibraryUserRepository libraryUserRepository) {
    this.keycloakUserSyncService = keycloakUserSyncService;
    this.libraryUserRepository = libraryUserRepository;
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new CurrentUserArgumentResolver(keycloakUserSyncService));
  }

  public static class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final KeycloakUserSyncService keycloakUserSyncService;

    public CurrentUserArgumentResolver(KeycloakUserSyncService keycloakUserSyncService) {
      this.keycloakUserSyncService = keycloakUserSyncService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
      return parameter.getParameterType().equals(LibraryUser.class)
          && parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory) {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication instanceof JwtAuthenticationToken) {
        return keycloakUserSyncService.syncUser();
      }

      return null;
    }
  }
}
