# Spring Boot 4.0 and Java 21 Migration Plan

## Overview

This document outlines the migration of the Omny Link CRM project from Spring Boot 3.3.2/Java 17 to Spring Boot 4.0.1/Java 21.

**Migration Date**: January 20, 2026  
**Status**: Implementation Complete - Testing Required

---

## ✅ Completed Changes

### 1. Development Environment

**File**: `.devcontainer/devcontainer.json`

- ✅ Updated Java: 17 → **21**
- ✅ Updated Maven: 3.9
- ✅ Removed PostgreSQL client (using embedded H2 for local development)
- ✅ Added comprehensive Java extensions:
  - VS Code Java Extension Pack
  - Maven for Java
  - Spring Boot extensions
  - Spring Boot Dashboard

### 2. Core Framework Versions

**File**: `pom.xml` (root)

- ✅ Spring Boot: 3.3.2 → **4.0.1**
- ✅ Java version: 17 → **21**
- ✅ Spring Framework: 6.1.5 → **7.0.0**
- ✅ Springdoc OpenAPI: 2.4.0 → **2.6.0**

### 3. Database Driver

**File**: `crm-server/pom.xml`

- ✅ PostgreSQL driver: [42.3.3,) → **42.7.4**
  - Java 21 compatible version
  - Fixed version for reproducible builds

### 4. Apache Commons Libraries

**Files**: `catalog/pom.xml`, `cust-mgmt/pom.xml`

- ✅ Commons CSV: 1.2 → **1.11.0**
  - 10 year update, includes bug fixes and security patches

### 5. HTML Parsing Library

**File**: `cust-mgmt/pom.xml`

- ✅ JSoup: 1.15.3 → **1.18.1**
  - Latest stable release with security improvements

### 6. Liquibase Schema

**File**: `crm-server/src/main/resources/liquibase-changelog.xml`

- ✅ Schema XSD: dbchangelog-3.10.xsd → **dbchangelog-4.29.xsd**
- ✅ Updated all schema location URLs to Liquibase 4.x

### 7. Virtual Threads (Java 21 Feature)

**File**: `crm-server/src/main/resources/application.properties`

- ✅ Added: `spring.threads.virtual.enabled=true`
- **Benefit**: Automatic performance improvements for I/O-bound operations
- **Note**: No code changes required - Spring Boot handles thread management

### 8. Nashorn JavaScript Engine

**Status**: ✅ Verified Compatible

- Using standalone `nashorn-core` 15.4 (not JDK bundled)
- Works with Java 21
- Used in `RestApiTest.java` for REST API testing

---

## 📋 Testing Checklist

### Step 1: Rebuild Devcontainer

```bash
# VS Code Command Palette (Ctrl+Shift+P or Cmd+Shift+P)
# Select: "Dev Containers: Rebuild Container"
```

### Step 2: Verify Maven Installation

```bash
mvn --version
# Expected output:
# Apache Maven 3.9.x
# Java version: 21
```

### Step 3: Clean Build

```bash
mvn clean compile -DskipTests
```

**Expected Result**: All modules compile successfully

### Step 4: Run Unit Tests

```bash
mvn test
```

**Watch for**:
- Nashorn-based REST API tests
- JPA/Hibernate compatibility
- Jackson JSON serialization

### Step 5: Run Integration Tests

```bash
mvn verify
```

**Watch for**:
- Spring Boot context loading
- Database migrations (Liquibase)
- REST endpoint availability

### Step 6: Start Application

```bash
cd crm-server
mvn spring-boot:run
```

**Verify**:
- Application starts without errors
- H2 console accessible (if enabled)
- API documentation: http://localhost:8080/swagger-ui.html
- Actuator endpoints: http://localhost:8080/actuator

### Step 7: Smoke Tests

Test key endpoints:
```bash
# Health check
curl http://localhost:8080/actuator/health

# API endpoints (adjust based on your APIs)
curl http://localhost:8080/api/v1/accounts
curl http://localhost:8080/api/v1/contacts
```

---

## 🔍 Known Migration Considerations

### Spring Boot 4.0 Breaking Changes (From Migration Guide)

1. **Module Dependencies**
   - Spring Boot 4.0 has new modular design
   - May need to add specific starter dependencies
   - If issues arise, can temporarily use `spring-boot-starter-classic`

2. **Jackson 3 (from Jackson 2)**
   - Spring Boot 4.0 uses Jackson 3
   - New group IDs: `com.fasterxml.jackson` → `tools.jackson`
   - Annotations unchanged: `com.fasterxml.jackson.annotation`
   - **If issues**: Can use `spring-boot-jackson2` module temporarily

3. **Test Dependencies**
   - `@MockBean`/`@SpyBean` → `@MockitoBean`/`@MockitoSpyBean`
   - May need to add test-specific starters
   - `@SpringBootTest` no longer provides MockMVC automatically
     - Add `@AutoConfigureMockMvc` if needed

4. **Properties Renamed**
   - ~~`spring.data.mongodb.*`~~ → `spring.mongodb.*` (some properties)
   - ~~`spring.session.redis.*`~~ → `spring.session.data.redis.*`
   - ~~`spring.dao.exceptiontranslation.enabled`~~ → `spring.persistence.exceptiontranslation.enabled`

5. **Virtual Threads**
   - Enabled in application.properties
   - Improves performance for blocking I/O
   - Monitor thread usage in logs

### Project-Specific Notes

1. **iText PDF Library**
   - Status: **Kept at version 5.5.13.3** (as requested)
   - Note: Version 7+ has license changes
   - Location: `pdf-service` module

2. **Spring Security**
   - Status: **Changes deferred** (separate migration planned)
   - Current: Keycloak OAuth2/JWT configured but not fully implemented
   - `AuditorAwareImpl` has hardcoded user: `"tstephen"`

3. **Database Configuration**
   - **Local Development**: H2 embedded database
   - **Production**: PostgreSQL 42.7.4
   - Liquibase enabled/disabled via properties

---

## 🚨 Troubleshooting Guide

### Compilation Errors

**Issue**: Missing dependencies or imports

**Solution**: Spring Boot 4.0 modularization may require explicit starters

```xml
<!-- Example: Add specific starter if needed -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

### Jackson Serialization Issues

**Issue**: JSON serialization/deserialization failures

**Temporary Fix**: Add Jackson 2 compatibility
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-jackson2</artifactId>
</dependency>
```

**Long-term**: Migrate to Jackson 3 (review migration guide)

### Test Failures

**Issue**: `@MockBean` deprecated warnings

**Solution**: Replace with `@MockitoBean`
```java
// Old
@MockBean
private MyService myService;

// New
@MockitoBean
private MyService myService;
```

**Issue**: MockMVC not available in `@SpringBootTest`

**Solution**: Add annotation
```java
@SpringBootTest
@AutoConfigureMockMvc
class MyControllerTest { ... }
```

### Liquibase Migration Issues

**Issue**: Schema validation errors

**Check**: Ensure all changelog files use 4.29.xsd schema
```bash
grep -r "dbchangelog-3" crm-server/src/main/resources/
# Should return no results
```

### Virtual Threads Issues

**Issue**: Performance degradation or threading problems

**Solution**: Disable virtual threads temporarily
```properties
# application.properties
spring.threads.virtual.enabled=false
```

---

## 📚 Reference Documentation

### Spring Boot 4.0 Resources

- [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)
- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Spring Framework 7.0 Release Notes](https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-7.0-Release-Notes)

### Java 21 Features

- [Java 21 Release Notes](https://openjdk.org/projects/jdk/21/)
- [Virtual Threads (Project Loom)](https://openjdk.org/jeps/444)
- [Pattern Matching Enhancements](https://openjdk.org/jeps/441)
- [Record Patterns](https://openjdk.org/jeps/440)

### Dependency Documentation

- [PostgreSQL JDBC 42.7.4](https://jdbc.postgresql.org/documentation/)
- [Liquibase 4.x](https://docs.liquibase.com/start/home.html)
- [Springdoc OpenAPI 2.6](https://springdoc.org/)

---

## 📝 Post-Migration Tasks

### Recommended (Not Required for Initial Testing)

1. **Code Modernization** (Optional)
   - Use Java 21 Records for DTOs
   - Apply Text Blocks for SQL/JSON strings
   - Use Pattern Matching where applicable
   - Consider Sealed Classes for domain hierarchies

2. **Security Implementation** (Separate Plan)
   - Implement actual Keycloak integration
   - Remove hardcoded user in `AuditorAwareImpl`
   - Add proper JWT validation
   - Configure role-based access control

3. **Dependency Cleanup**
   - Review and update `commons-io` (currently 2.14.0)
   - Consider iText alternatives if licensing is a concern
   - Update Batik if needed (currently 1.16)

4. **Performance Tuning**
   - Monitor virtual thread usage
   - Optimize database queries for new thread model
   - Review connection pool settings

5. **Documentation Updates**
   - Update README.md with new versions
   - Document virtual threads configuration
   - Update deployment guides for Java 21

---

## ✅ Migration Sign-Off

**Completed By**: GitHub Copilot  
**Reviewed By**: _[Your Name]_  
**Testing Completed**: _[Date]_  
**Deployed to Production**: _[Date]_  

### Test Results

- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Application starts successfully
- [ ] API endpoints functional
- [ ] Virtual threads working
- [ ] No performance regressions
- [ ] Documentation updated

---

## 🆘 Support

If you encounter issues during migration:

1. Check this document's troubleshooting section
2. Review Spring Boot 4.0 Migration Guide (linked above)
3. Check GitHub Issues: https://github.com/spring-projects/spring-boot/issues
4. Stack Overflow tag: `spring-boot-4`

**Good luck with the migration!** 🚀
