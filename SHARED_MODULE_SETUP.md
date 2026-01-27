# Shared Module Setup

## Overview

A shared module has been created to eliminate code duplication across microservices. This module contains common DTOs, utilities, exception handlers, configurations, aspects, annotations, and MongoDB documents.

## Shared Module Structure

```
shared-module/
├── build.gradle
├── settings.gradle
└── src/main/java/com/service/shared/
    ├── dto/
    │   └── GlobalResponse.java
    ├── util/
    │   └── ResponseHelper.java
    ├── exception/
    │   ├── GlobalException.java
    │   └── GlobalExceptionHandler.java
    ├── entity/
    │   └── User.java
    ├── repository/
    │   ├── UserRepository.java
    │   └── ApiLogRepository.java
    ├── document/
    │   └── ApiLog.java
    ├── annotation/
    │   └── MethodCode.java
    ├── aspect/
    │   └── ApiLoggingAspect.java
    └── config/
        ├── OAuth2ResourceServerConfig.java
        ├── BaseSecurityConfig.java
        ├── WebClientConfig.java
        ├── KafkaConfig.java
        ├── MongoConfig.java
        └── ApplicationConfig.java
```

## What's Included

### 1. DTOs
- **GlobalResponse**: Standardized response format for all microservices

### 2. Utilities
- **ResponseHelper**: Helper methods for creating standardized responses

### 3. Exceptions
- **GlobalException**: Custom exception with error codes
- **GlobalExceptionHandler**: Global exception handler for all microservices

### 4. Entities
- **User**: JPA entity for user management (used across multiple services)

### 5. Repositories
- **UserRepository**: JPA repository for User entity with common query methods

### 6. Repositories (MongoDB)
- **ApiLogRepository**: MongoDB repository for API logs with query methods

### 7. Documents (MongoDB)
- **ApiLog**: MongoDB document for storing API request/response logs

### 8. Annotations
- **MethodCode**: Annotation for specifying unique method codes for API logging

### 9. Aspects
- **ApiLoggingAspect**: AOP aspect that automatically logs all API requests and responses to MongoDB

### 10. Configurations
- **OAuth2ResourceServerConfig**: Base OAuth2 configuration with JWT decoder
- **BaseSecurityConfig**: Helper class for creating SecurityFilterChain
- **WebClientConfig**: Configuration for WebClient (internal and Nokia API)
- **KafkaConfig**: Kafka producer and consumer configuration
- **MongoConfig**: MongoDB connection configuration
- **ApplicationConfig**: Common application beans (ObjectMapper, etc.)

## How to Use in Microservices

### 1. Update build.gradle

Add the shared module dependency:

```gradle
dependencies {
    // Shared Module
    implementation project(':shared-module')
    
    // ... other dependencies
}
```

### 2. Update settings.gradle

Include the shared module:

```gradle
rootProject.name = 'your-service-name'

include 'shared-module'
project(':shared-module').projectDir = file('../shared-module')
```

### 3. Update Imports

Replace local imports with shared module imports:

**Before:**
```java
import com.service.yourservice.dto.GlobalResponse;
import com.service.yourservice.util.ResponseHelper;
import com.service.yourservice.exception.GlobalException;
import com.service.yourservice.entity.User;
import com.service.yourservice.repository.UserRepository;
```

**After:**
```java
import com.service.shared.dto.GlobalResponse;
import com.service.shared.util.ResponseHelper;
import com.service.shared.exception.GlobalException;
import com.service.shared.entity.User;
import com.service.shared.repository.UserRepository;
```

### 4. Update OAuth2 Configuration

**Before:**
```java
@Configuration
@EnableWebSecurity
public class OAuth2ResourceServerConfig {
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ... configuration
    }
}
```

**After:**
```java
@Configuration
@EnableWebSecurity
public class YourServiceOAuth2ResourceServerConfig extends OAuth2ResourceServerConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/health", "/your-service/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder))
            );
        return http.build();
    }
}
```

### 5. Remove Duplicate Files

Delete these files from your microservice (they're now in shared-module):
- `src/main/java/com/service/yourservice/dto/GlobalResponse.java`
- `src/main/java/com/service/yourservice/util/ResponseHelper.java`
- `src/main/java/com/service/yourservice/exception/GlobalException.java`
- `src/main/java/com/service/yourservice/exception/GlobalExceptionHandler.java`
- `src/main/java/com/service/yourservice/entity/User.java` (if applicable)
- `src/main/java/com/service/yourservice/repository/UserRepository.java` (if applicable)
- `src/main/java/com/service/yourservice/aspect/ApiLoggingAspect.java`
- `src/main/java/com/service/yourservice/annotation/MethodCode.java`
- `src/main/java/com/service/yourservice/document/ApiLog.java`
- `src/main/java/com/service/yourservice/repository/ApiLogRepository.java`
- `src/main/java/com/service/yourservice/config/WebClientConfig.java`
- `src/main/java/com/service/yourservice/config/KafkaConfig.java`
- `src/main/java/com/service/yourservice/config/MongoConfig.java`
- `src/main/java/com/service/yourservice/config/ApplicationConfig.java`

**Note:** 
- Keep your service-specific OAuth2ResourceServerConfig but extend the shared one.
- The ApiLoggingAspect automatically works with any controller in any service package.
- Update imports to use `com.service.shared.*` for all shared components.

## Services Updated

- ✅ **connectivityService**: Fully migrated to shared module (including aspects and configs)
- ✅ **auth-service**: Fully migrated to shared module (including User entity and repository)
- ✅ **identification-service**: Configs removed, using shared module
- ✅ **locationService**: Configs removed, using shared module
- ✅ **deviceManagementService**: Configs removed, using shared module
- ✅ **ai-agent-service**: Configs removed, using shared module
- ✅ **apiGateway**: Configs removed, using shared module

## Migration Checklist

For each microservice:

- [ ] Update `build.gradle` to include shared-module dependency
- [ ] Update `settings.gradle` to include shared-module project
- [ ] Update all imports to use `com.service.shared.*`
- [ ] Update OAuth2ResourceServerConfig to extend shared config
- [ ] Delete duplicate DTO, util, and exception files
- [ ] Test compilation: `./gradlew compileJava`
- [ ] Test application startup

## Benefits

1. **Code Reusability**: Single source of truth for common code
2. **Consistency**: All services use the same response format and exception handling
3. **Maintainability**: Bug fixes and improvements in one place benefit all services
4. **Reduced Duplication**: Less code to maintain and test
