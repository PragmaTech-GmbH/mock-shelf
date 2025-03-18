# Mock Shelf

- [ ] Docker Compose: Local Mailing, Mocked Remotes

## Setup

```shell
pre-commit install
```

## Keycloak

Export realm:

```
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

# MockShelf Infrastructure Architecture

## External Infrastructure Components

```mermaid
graph TD
    subgraph "External Services & Infrastructure"
        KC[" 🔐 Keycloak\nAuthentication & Authorization"]
        PG[" 🐘 PostgreSQL\nPersistent Data Storage"]
        AMQ[" 📬 ActiveMQ\nMessage Queuing"]
        MD[" 📧 MailDev\nEmail Testing"]
        WM[" 🌐 WireMock\nExternal API Simulation"]
        JR[" 🏃 JobRunr\nBackground Job Processing"]
        ISBN[" 📖 Open Library API\nISBN Book Lookup"]
    end

    subgraph "MockShelf Application"
        APP[" 🚀 Spring Boot App\nCore Application Logic"]
    end

    subgraph "Development & CI/CD"
        GH[" 🐙 GitHub\nSource Control"]
        DC[" 🐳 Docker Compose\nLocal Environment"]
        PC[" 🧪 Pre-commit\nCode Quality Checks"]
        MVN[" 🌟 Maven\nDependency Management"]
    end

    subgraph "Monitoring & Observability"
        ACT[" 🔍 Spring Actuator\nSystem Monitoring"]
        LOG[" 📊 SLF4J Logging\nApplication Logs"]
    end

    APP <-->|Authentication| KC
    APP <-->|Persistent Storage| PG
    APP <-->|Messaging| AMQ
    APP <-->|Job Processing| JR
    APP <-->|Book Lookup| ISBN

    DC <-->|Manages| KC
    DC <-->|Manages| PG
    DC <-->|Manages| AMQ
    DC <-->|Manages| MD
    DC <-->|Manages| WM

    GH <-->|CI/CD| APP
    MVN <-->|Dependency Management| APP

    APP <-->|Monitoring| ACT
    APP <-->|Logging| LOG
```

## Infrastructure Components Detailed Description

### Authentication & Authorization
- **Keycloak** 🔐
  - Open-source identity and access management
  - Provides OAuth 2.0 and OpenID Connect
  - Handles user authentication, authorization, and user management

### Data Storage
- **PostgreSQL** 🐘
  - Robust, open-source relational database
  - Stores books, loans, users, and other library-related data
  - Supports complex queries and transactions

### Messaging & Queuing
- **ActiveMQ** 📬
  - Message broker for asynchronous communication
  - Enables event-driven notifications
  - Supports complex messaging patterns

### Email & Testing
- **MailDev** 📧
  - SMTP testing server
  - Captures and displays outgoing emails
  - Helps in development and testing email functionality

### API Mocking
- **WireMock** 🌐
  - Lightweight API mocking tool
  - Simulates external service responses
  - Enables predictable testing of external integrations

### Background Processing
- **JobRunr** 🏃
  - Distributed background job processing
  - Manages and monitors long-running tasks
  - Provides job scheduling and retry mechanisms

### Book Lookup
- **Open Library API** 📖
  - Free, open-source book information API
  - Provides book details based on ISBN
  - Enriches book catalog with external metadata

### Development & CI/CD
- **GitHub** 🐙
  - Source code management
  - Workflow automation
  - Continuous integration

- **Docker Compose** 🐳
  - Local environment orchestration
  - Simplifies service dependencies
  - Ensures consistent development setup

- **Pre-commit** 🧪
  - Code quality checks
  - Enforces coding standards
  - Runs linters and formatters before commits

- **Maven** 🌟
  - Dependency management
  - Build automation
  - Project structure and plugin management

### Monitoring & Observability
- **Spring Actuator** 🔍
  - Provides production-ready features
  - Health checks and metrics
  - Exposes operational information

- **SLF4J Logging** 📊
  - Standardized logging facade
  - Configurable log levels
  - Supports multiple logging implementations

## Design Principles

1. **Modularity**: Each component has a clear, focused responsibility
2. **Testability**: Easy to mock and test individual components
3. **Scalability**: Loosely coupled services
4. **Developer Experience**: Tools that enhance productivity

## Deployment Considerations

- Local development with Docker Compose
- Potential Kubernetes deployment for scaling
- Stateless design for horizontal scaling
- Centralized configuration management
