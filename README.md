# Mock Shelf

- [ ] Docker Compose: Local Mailing, Mocked Remotes

## Setup

```shell
pre-commit install
```

## MockShelf Architecture Documentation

## Overview

MockShelf is a comprehensive library management system designed for testing and development purposes. It demonstrates modern Spring Boot application architecture with a focus on testing techniques and microservice principles.

## System Context

```mermaid
C4Context
    title System Context Diagram for MockShelf Library Management System

    Person(librarian, "Librarian", "Manages library resources and user accounts")
    Person(reader, "Library User", "Browses and borrows books")

    System(mockshelf, "MockShelf", "Library management system for book lending and tracking")

    Rel(librarian, mockshelf, "Manages")
    Rel(reader, mockshelf, "Uses")
```

## Container Diagram

```mermaid
C4Container
    title Container Diagram for MockShelf

    Person(reader, "Library User")
    Person(librarian, "Librarian")

    System_Boundary(mockshelf, "MockShelf") {
        Container(web_app, "Web Application", "Spring Boot, Thymeleaf, HTMX", "Provides user interfaces for book browsing, borrowing, and management")
        Container(auth_service, "Authentication Service", "Keycloak", "Handles user authentication and authorization")
        Container(book_service, "Book Service", "Spring Boot", "Manages book catalog and inventory")
        Container(loan_service, "Loan Service", "Spring Boot", "Manages book loans and returns")
        Container(notification_service, "Notification Service", "Spring Boot", "Handles email and SMS notifications")

        ContainerDb(postgres, "Database", "PostgreSQL", "Stores library data")
        ContainerDb(activemq, "Message Broker", "ActiveMQ", "Handles asynchronous messaging")
    }

    System_Ext(email_system, "Email System")
    System_Ext(isbn_lookup, "ISBN Lookup Service")

    Rel(reader, web_app, "Interacts with")
    Rel(librarian, web_app, "Manages")
    Rel(web_app, auth_service, "Authenticates")
    Rel(web_app, book_service, "Manages books")
    Rel(web_app, loan_service, "Manages loans")

    Rel(book_service, postgres, "Reads/Writes")
    Rel(loan_service, postgres, "Reads/Writes")
    Rel(notification_service, activemq, "Consumes/Publishes messages")
    Rel(notification_service, email_system, "Sends emails")
    Rel(book_service, isbn_lookup, "Looks up book details")
```

## Component Diagram for Book Service

```mermaid
C4Component
    title Component Diagram for Book Service

    Container(book_service, "Book Service", "Spring Boot", "Manages book catalog and inventory")

    Component(book_controller, "Book Controller", "REST Controller", "Handles HTTP requests for book operations")
    Component(book_service_component, "Book Service", "Service Layer", "Business logic for book management")
    Component(book_repository, "Book Repository", "JPA Repository", "Handles database interactions")
    Component(isbn_lookup_service, "ISBN Lookup Service", "External Service Integration", "Fetches book details from external API")

    Rel(book_controller, book_service_component, "Uses")
    Rel(book_service_component, book_repository, "Persists")
    Rel(book_service_component, isbn_lookup_service, "Retrieves book details")
```

## Architecture Characteristics

### Key Architecture Drivers
- Testability
- Modularity
- Security
- Performance

### Technology Stack
- **Backend**: Spring Boot 3.4.3
- **Authentication**: Keycloak
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA
- **Messaging**: ActiveMQ
- **Frontend**: Thymeleaf, HTMX, Alpine.js
- **Testing**: JUnit 5, Testcontainers

### Architectural Patterns
- Microservices-like architecture
- Repository pattern
- Event-driven notifications
- Resource server authentication

## Deployment

### Docker Composition
MockShelf uses Docker Compose for local development, spinning up:
- Application container
- PostgreSQL database
- Keycloak authentication
- ActiveMQ message broker
- MailDev for email testing
- WireMock for external service mocking

## Testing Strategy

### Testing Pyramid
- Unit Tests: Extensive coverage of service and repository layers
- Integration Tests: Using Testcontainers for realistic environment
- Web Tests: HTMX and Thymeleaf rendering checks
- End-to-End Tests: Simulated user journeys

### Key Testing Technologies
- JUnit 5
- Testcontainers
- Selenium
- Mockito
- Spring Boot Test

## Security Considerations
- OAuth 2.0 Resource Server
- Role-based access control
- JWT token authentication
- CSRF protection
- HTTPS enforcement

## Monitoring and Observability
- Spring Boot Actuator
- JobRunr for background job tracking
- Logging with SLF4J
- Potential integration with Prometheus/Grafana

## Future Improvements
- Microservices decomposition
- Enhanced caching strategies
- More granular authorization
- Enhanced monitoring
