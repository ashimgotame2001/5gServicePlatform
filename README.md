# Smart 5G Service Platform

A comprehensive Java backend platform leveraging Nokia's Network as Code platform and advanced 5G APIs to build real-world applications and services.

## Architecture Overview

The platform consists of 6 microservices:

1. **API Gateway** (Port 8080) - Single entry point with routing, authentication, and rate limiting
2. **Auth Service** (Port 8085) - Authentication and authorization provider, user management
3. **Connectivity Service** (Port 8081) - Manages network slices and QoS
4. **Identification Service** (Port 8082) - Handles number verification and KYC checks
5. **Location Service** (Port 8083) - Provides location verification and geo-fencing
6. **Device Management Service** (Port 8084) - Manages SIM and device swaps

## Technology Stack

- **Framework**: Spring Boot 4.0.2
- **Java Version**: 21
- **API Gateway**: Spring Cloud Gateway
- **Database**: PostgreSQL (relational data), MongoDB (logs/telemetry)
- **Messaging**: Apache Kafka
- **Security**: Spring Security + OAuth2/JWT
- **HTTP Client**: WebClient (Reactive)
- **Monitoring**: Spring Boot Actuator + Prometheus

## Prerequisites

- Java 21 or higher
- PostgreSQL 12+ 
- MongoDB 4.4+
- Apache Kafka 2.8+
- Gradle 8.0+ (or use included Gradle wrapper)

## Setup Instructions

### 1. Database Setup

#### PostgreSQL
```sql
CREATE DATABASE smart_5g_db;
CREATE USER postgres WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE smart_5g_db TO postgres;
```

#### MongoDB
MongoDB will be used automatically for logs and telemetry. Ensure MongoDB is running on `localhost:27017`.

### 2. Kafka Setup

Start Kafka and Zookeeper:
```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

### 3. Environment Variables Setup

Each microservice uses `.env` files for configuration. You can set them up in two ways:

#### Option 1: Automated Setup (Recommended)

Run the setup script to create all `.env` files:

```bash
chmod +x setup-env.sh
./setup-env.sh
```

Then update each `.env` file with your actual values.

#### Option 2: Manual Setup

Create a `.env` file in each service directory with the following variables:

**For all services (except API Gateway):**
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=smart_5g_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

# MongoDB Configuration
MONGO_HOST=localhost
MONGO_PORT=27017
MONGO_DB=smart_5g_logs

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Nokia Network as Code API
NOKIA_NAC_API_KEY=your_nokia_api_key
NOKIA_NAC_BASE_URL=https://api.networkascode.nokia.io
```

**For API Gateway:**
```bash
# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# JWT Configuration
JWT_SECRET=your-secret-key-change-in-production
JWT_JWK_SET_URI=https://your-auth-provider.com/.well-known/jwks.json
```

📖 **See [ENV_SETUP.md](ENV_SETUP.md) for detailed instructions and troubleshooting.**

> **Note**: The `.env` files are automatically loaded by each service using the `DotEnvConfig` class. Make sure to add `.env` to `.gitignore` to avoid committing sensitive information.

### 4. Build and Run Services

#### Build all services:
```bash
# API Gateway
cd apiGateway && ./gradlew build

# Connectivity Service
cd connectivityService && ./gradlew build

# Identification Service
cd identification-service && ./gradlew build

# Location Service
cd locationService && ./gradlew build

# Device Management Service
cd deviceManagementService && ./gradlew build
```

#### Run services (in separate terminals):
```bash
# API Gateway
cd apiGateway && ./gradlew bootRun

# Auth Service (start this first)
cd auth-service && ./gradlew bootRun

# Connectivity Service
cd connectivityService && ./gradlew bootRun

# Identification Service
cd identification-service && ./gradlew bootRun

# Location Service
cd locationService && ./gradlew bootRun

# Device Management Service
cd deviceManagementService && ./gradlew bootRun
```

### 5. Authentication Setup

1. **Register a user:**
   ```bash
   curl -X POST http://localhost:8080/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser",
       "email": "test@example.com",
       "password": "password123"
     }'
   ```

2. **Login to get JWT token:**
   ```bash
   curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser",
       "password": "password123"
     }'
   ```

3. **Use the token for protected endpoints:**
   ```bash
   curl -X GET http://localhost:8080/connectivity/status \
     -H "Authorization: Bearer <your-jwt-token>"
   ```

**Important**: Make sure the `JWT_SECRET` in both `apiGateway/.env` and `auth-service/.env` match!

## API Endpoints

### API Gateway (Port 8080)

All requests should go through the API Gateway:

- `GET /actuator/health` - Health check
- `GET /actuator/metrics` - Metrics endpoint
- `POST /connectivity/qos-request` - Request QoS adjustment
- `GET /connectivity/status` - Get connectivity status
- `POST /identification/verify-number` - Verify phone number
- `POST /identification/kyc-check` - Perform KYC check
- `GET /identification/device-status` - Get device status
- `GET /location/verify` - Verify location
- `GET /location/geofence-alerts` - Get geo-fence alerts
- `POST /device/swap-sim` - Swap SIM card
- `POST /device/swap-device` - Swap device

## Configuration Details

### WebClient Configuration

Each service has a `WebClientConfig` that configures:
- Base URL for Nokia Network as Code APIs
- Timeout settings (default: 30 seconds)
- Default headers (Content-Type, Accept)

### Kafka Configuration

Each service includes:
- Producer factory with JSON serialization
- Consumer factory with JSON deserialization
- Kafka template for sending messages
- Listener container factory for receiving messages

### Nokia Network as Code Client

Each service has a `NokiaNacClient` service that:
- Handles authentication with Bearer tokens
- Implements retry logic (configurable, default: 3 attempts)
- Provides type-safe methods for API calls
- Handles timeouts and error scenarios

## Security

- API Gateway implements OAuth2 Resource Server with JWT validation
- All microservices use Spring Security
- JWT tokens are validated at the gateway level
- Circuit breakers protect against cascading failures

## Monitoring

- Spring Boot Actuator endpoints available at `/actuator`
- Prometheus metrics at `/actuator/prometheus`
- Health checks at `/actuator/health`
- Gateway routes monitoring at `/actuator/gateway/routes`

## Circuit Breakers

Resilience4j circuit breakers are configured for:
- Connectivity Service
- Identification Service
- Location Service
- Device Management Service

Configuration:
- Sliding window size: 10
- Minimum number of calls: 5
- Failure rate threshold: 50%
- Wait duration in open state: 5 seconds

## Development Notes

### Adding New Nokia API Endpoints

1. Add endpoint configuration to `application.yaml`:
```yaml
nokia:
  nac:
    your-service:
      endpoint: /v1/your-service/endpoint
```

2. Add method to `NokiaNacClient`:
```java
public Mono<Map<String, Object>> yourMethod(Map<String, Object> request) {
    return webClient.post()
            .uri(baseUrl + "/v1/your-service/endpoint")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Map.class)
            .cast(Map.class)
            .map(map -> (Map<String, Object>) map)
            .retryWhen(Retry.fixedDelay(retryAttempts, Duration.ofSeconds(2)))
            .timeout(Duration.ofSeconds(30));
}
```

## Troubleshooting

### Service won't start
- Check database connection settings
- Verify Kafka is running
- Check port conflicts
- Review application logs

### API Gateway routing issues
- Verify service ports match configuration
- Check circuit breaker status
- Review gateway logs at `/actuator/gateway/routes`

### Nokia API calls failing
- Verify API key is correct
- Check network connectivity
- Review retry configuration
- Check Nokia API documentation for endpoint changes

## License

This project is part of the Smart 5G Service Platform.
# 5gServicePlatform
# 5gServicePlatform
