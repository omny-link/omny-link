# Copilot Instructions for CRM Backend (Java)

## Project Overview

This is a Java-based CRM backend system with multiple Spring Boot modules providing REST APIs for account management, contacts, and other CRM functionality.

## Technology Stack

- **Framework**: Spring Boot 4.x
- **Language**: Java 21+
- **Build Tool**: Maven
- **Security**: Spring Security with Keycloak OAuth2/JWT
- **Database**: JPA/Hibernate with relational database
- **API**: RESTful services
- **Testing**: JUnit 5, Mockito, Spring Boot Test

## Code Style & Conventions

### Java Code Style

- Follow Java naming conventions (camelCase for methods/variables, PascalCase for classes)
- Use meaningful, descriptive variable and method names
- Keep methods short and focused (single responsibility principle)
- Use Java 21+ features: records, pattern matching, text blocks, sealed classes
- Prefer immutability where possible (final fields, unmodifiable collections)
- Use Lombok annotations to reduce boilerplate (`@Data`, `@Builder`, `@RequiredArgsConstructor`)

### Package Structure

```
com.knowprocess.crm/
├── controller/      # REST controllers (@RestController)
├── service/         # Business logic (@Service)
├── repository/      # Data access layer (@Repository, JPA)
├── model/          # Domain entities (@Entity)
│   └── entity/     # JPA entities
├── dto/            # Data Transfer Objects (records or POJOs)
├── mapper/         # Entity ↔ DTO mappers
├── config/         # Configuration classes (@Configuration)
├── security/       # Security configuration (Keycloak, JWT)
├── exception/      # Custom exceptions and handlers
└── util/           # Utility classes and helpers
```

### Spring Boot Conventions

- **Dependency Injection**: Use constructor injection (required) over field injection
- **Controllers**: Annotate with `@RestController` and `@RequestMapping("/api/v1/...")`
- **Services**: Annotate with `@Service`, use `@Transactional` appropriately
- **Repositories**: Extend `JpaRepository<Entity, ID>`, annotate with `@Repository`
- **Configuration**: Use `@Configuration` classes, `@Bean` methods
- **Properties**: Use `@Value` or `@ConfigurationProperties` for external configuration

### REST API Design

- Use standard HTTP methods: GET (read), POST (create), PUT (replace), PATCH (update), DELETE (remove)
- Return appropriate HTTP status codes (200, 201, 204, 400, 401, 403, 404, 500)
- Use `/api/v1/` prefix for all API endpoints
- Use plural nouns for resources (e.g., `/api/v1/accounts`, `/api/v1/contacts`)
- Return DTOs, never expose entities directly
- Use `ResponseEntity<T>` for explicit status control
- Implement HATEOAS links where appropriate

### Security (Keycloak)

- Secure all endpoints except explicitly public ones
- Use `@PreAuthorize` for method-level security (e.g., `@PreAuthorize("hasRole('USER')")`)
- Validate JWT tokens from Keycloak
- Extract user context from SecurityContext
- Check roles and permissions before operations
- Never expose sensitive data in responses
- Log security events

### Database & JPA

- Use JPA entities with proper relationships (`@OneToMany`, `@ManyToOne`, `@ManyToMany`)
- Add indexes for frequently queried fields (`@Index`)
- Use `@Transactional` on service methods (read-only when appropriate)
- Write custom queries with `@Query` (JPQL or native SQL)
- Use database migrations (Flyway or Liquibase)
- Implement soft deletes where needed
- Use optimistic locking (`@Version`) for concurrency

### DTOs and Mapping

- Use Java records for immutable DTOs (Java 17+)
- Separate request DTOs from response DTOs
- Use MapStruct or manual mappers (avoid automatic mapping libraries like ModelMapper)
- Never expose internal IDs or sensitive fields
- Validate DTOs with Bean Validation annotations

### Error Handling

- Create custom exception classes extending `RuntimeException`
- Use `@ControllerAdvice` for global exception handling
- Return consistent error response structure (ErrorResponse DTO)
- Include error code, message, timestamp, and path
- Log errors with appropriate levels (ERROR, WARN, INFO)
- Include correlation IDs for request tracing
- Handle validation errors with `@ExceptionHandler(MethodArgumentNotValidException.class)`

### Logging

- Use SLF4J with Logback
- Log levels: ERROR (exceptions), WARN (concerning events), INFO (important flow), DEBUG (detailed info)
- Include context: user ID, request ID, entity ID
- Avoid logging sensitive data (passwords, tokens, PII)
- Use structured logging where possible

### Validation

- Use Bean Validation annotations: `@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Pattern`
- Validate at controller level with `@Valid` or `@Validated`
- Create custom validators for complex rules (`@Constraint`)
- Return validation errors in standardized format

### Testing

- Write unit tests with JUnit 5 and Mockito
- Integration tests with `@SpringBootTest` and `@AutoConfigureMockMvc`
- Test REST endpoints with MockMvc or RestAssured
- Mock external dependencies (Keycloak, databases)
- Use `@DataJpaTest` for repository tests
- Aim for >80% code coverage
- Test edge cases and error conditions

### Common Patterns

#### REST Controller

```java
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountDto>> getAllAccounts() {
        log.debug("Fetching all accounts");
        return ResponseEntity.ok(accountService.findAll());
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountDto dto) {
        log.info("Creating account: {}", dto.getName());
        AccountDto created = accountService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.findById(id));
    }
}
```

#### Service Layer

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public List<AccountDto> findAll() {
        return accountRepository.findAll().stream()
            .map(accountMapper::toDto)
            .toList();
    }

    @Transactional
    public AccountDto create(AccountDto dto) {
        Account entity = accountMapper.toEntity(dto);
        Account saved = accountRepository.save(entity);
        log.info("Created account with ID: {}", saved.getId());
        return accountMapper.toDto(saved);
    }

    public AccountDto findById(Long id) {
        return accountRepository.findById(id)
            .map(accountMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }
}
```

#### Exception Handling

```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            LocalDateTime.now(),
            errors
        );
        return ResponseEntity.badRequest().body(error);
    }
}
```

#### JPA Entity

```java
@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_account_name", columnList = "name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    private String name;

    @Email
    private String email;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contact> contacts = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
```

## Best Practices

- Follow SOLID principles
- Write clean, self-documenting code with meaningful names
- Use design patterns appropriately (Factory, Builder, Strategy, etc.)
- Document public APIs with JavaDoc
- Externalize configuration (application.yml, environment variables)
- Use Spring profiles for environment-specific config (dev, test, prod)
- Implement health checks and metrics (Spring Boot Actuator)
- Use caching strategically (`@Cacheable`)
- Implement pagination for large datasets (`Pageable`)
- Use async processing for long-running operations (`@Async`)

## Module-Specific Notes

- Each module should be independently deployable
- Share common code via a `common` or `core` module
- Use Spring Cloud for microservices patterns if needed
- Configure module-specific properties in `application.yml`
- Document inter-module dependencies
- Use API versioning (`/api/v1/`, `/api/v2/`)

## Documentation

- Use Springdoc OpenAPI for API documentation
- Access Swagger UI at `/swagger-ui.html`
- Document complex business logic with comments
- Maintain README.md for each module
- Document configuration properties
