# Smart 5G Service Platform

A comprehensive Java backend platform leveraging Nokia's Network as Code platform and advanced 5G APIs to build real-world applications and services. Features **intelligent AI agents** that autonomously monitor, analyze, and optimize 5G network performance in real-time.

## Overview

The Smart 5G Service Platform is a microservices-based solution that uses Nokia's Network as Code APIs to create autonomous AI agents capable of solving real-world problems across smart cities, emergency services, healthcare, transportation, and public safety.

### Key Features

- **Intelligent AI Agents**: 9 autonomous agents that use real network data to solve problems
- **Complete API Integration**: All Nokia Network as Code APIs integrated with OAuth2 authorization
- **Dynamic OAuth2**: Automatic client credentials retrieval and token management
- **Real-World Use Cases**: Smart cities, emergency connectivity, healthcare, transportation, public safety
- **Enterprise Security**: OAuth2 Authorization Server with JWT tokens
- **Real-Time Monitoring**: Continuous network data collection and analysis
- **Autonomous Decision Making**: Rule-based decision engine with ML-ready architecture

## Architecture Overview

The platform consists of **7 microservices**:

1. **API Gateway** (Port 8080) - Single entry point with routing, authentication, and rate limiting
2. **Auth Service** (Port 8085) - OAuth2 Authorization Server, user management
3. **Connectivity Service** (Port 8081) - Manages network slices and QoS on Demand
4. **Identification Service** (Port 8082) - Handles number verification, KYC checks, device status, SIM swap detection
5. **Location Service** (Port 8083) - Provides location verification, geofencing, population density analysis
6. **Device Management Service** (Port 8084) - Manages SIM and device swaps
7. **Decision Engine Service** (Port 8086) - **Intelligent decision-making agents**

## Technology Stack

- **Framework**: Spring Boot 4.0.2
- **Java Version**: 21
- **API Gateway**: Spring Cloud Gateway
- **Database**: PostgreSQL (relational data), MongoDB (logs/telemetry)
- **Messaging**: Apache Kafka
- **Security**: Spring Security + OAuth2/JWT
- **HTTP Client**: WebClient (Reactive)
- **Monitoring**: Spring Boot Actuator + Prometheus
- **APIs**: Nokia Network as Code (RapidAPI)

## Prerequisites

- Java 21 or higher
- PostgreSQL 12+ 
- MongoDB 4.4+ (or Docker with provided configuration)
- Apache Kafka 2.8+
- Gradle 8.0+ (or use included Gradle wrapper)

## Quick Start

### 0. Postman Collection

Import the Postman collection to test all API endpoints:
- **Collection**: `5G-Service-Platform.postman_collection.json`
- **Environment**: `5G-Service-Platform.postman_environment.json` (optional)

The collection includes:
- All authentication endpoints
- All service endpoints (Connectivity, Identification, Location, Device Management)
- Decision Engine endpoints
- Nokia NAC Metadata endpoints
- Monitoring and actuator endpoints

**Note**: Set the `baseUrl` variable to `http://localhost:8080` (or your API Gateway URL) in the Postman environment.

### 1. Database Setup

#### PostgreSQL
```sql
CREATE DATABASE smart_5g_db;
CREATE USER postgres WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE smart_5g_db TO postgres;
```

#### MongoDB (Docker)
```yaml
# docker-compose.yml
mongodb:
  image: mongo:7.0
  container_name: mongodb
  restart: unless-stopped
  ports:
    - "27017:27017"
  environment:
    MONGO_INITDB_ROOT_USERNAME: admin
    MONGO_INITDB_ROOT_PASSWORD: admin123
    MONGO_INITDB_DATABASE: mydb
  volumes:
    - mongo_data:/data/db
```

### 2. Kafka Setup

Start Kafka and Zookeeper:
```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

### 3. Build and Run Services

#### Build all services:
```bash
# API Gateway
cd apiGateway && ./gradlew build

# Auth Service
cd auth-service && ./gradlew build

# Connectivity Service
cd connectivityService && ./gradlew build

# Identification Service
cd identification-service && ./gradlew build

# Location Service
cd locationService && ./gradlew build

# Device Management Service
cd deviceManagementService && ./gradlew build

# Decision Engine Service
cd decision-engine-service && ./gradlew build
```

#### Run services (in separate terminals):
```bash
# 1. Start Auth Service first (required for OAuth2)
cd auth-service && ./gradlew bootRun

# 2. Start API Gateway
cd apiGateway && ./gradlew bootRun

# 3. Start other services
cd connectivityService && ./gradlew bootRun
cd identification-service && ./gradlew bootRun
cd locationService && ./gradlew bootRun
cd deviceManagementService && ./gradlew bootRun

# 4. Start Decision Engine Service
cd decision-engine-service && ./gradlew bootRun
```

### 4. Nokia NAC Configuration

The platform automatically retrieves OAuth2 client credentials from Nokia NAC authorization server. You only need to configure the RapidAPI key:

```yaml
# In each service's application.yaml
nokia:
  nac:
    rapidapi-key: your-rapidapi-key-here
    rapidapi-host: network-as-code.nokia.rapidapi.com
    authorization-server-url: https://authorization.p-eu.rapidapi.com
    scope: read write
    timeout: 30000
    retry-attempts: 3
```

**Note**: Client credentials (`client_id` and `client_secret`) are automatically retrieved from Nokia NAC authorization server - no manual configuration needed!

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

## Decision Engine Agents

The platform includes **9 intelligent decision-making agents** that autonomously solve real-world problems:

### Core Agents

1. **QoS Optimization Agent** - Autonomously optimizes Quality of Service based on real-time network conditions
2. **Network Monitoring Agent** - Continuously monitors network conditions and detects anomalies
3. **Location Verification Agent** - Autonomously verifies and manages location data
4. **Device Management Agent** - Autonomously manages device and SIM card operations

### Use Case Agents

5. **Smart City Agent** - Monitors city infrastructure (traffic lights, sensors, cameras) - CityCare-like
6. **Emergency Connectivity Agent** - Guarantees connectivity for emergency services
7. **Healthcare Monitoring Agent** - Ensures reliable connectivity for remote patient monitoring
8. **Transportation Agent** - Manages connectivity for transportation and event logistics
9. **Public Safety Agent** - Monitors public safety systems and sustainability

### Execute Decision Engine Agents

```bash
# Execute all agents for a device
curl -X POST http://localhost:8080/decision-engine/execute/+1234567890 \
  -H "Authorization: Bearer <token>"

# Execute individual agent
curl -X POST http://localhost:8080/decision-engine/execute/{agentId}/+1234567890 \
  -H "Authorization: Bearer <token>"

# Available agent IDs:
# - qos-optimization-agent
# - network-monitoring-agent
# - location-verification-agent
# - device-management-agent
# - smart-city-agent
# - emergency-connectivity-agent
# - healthcare-monitoring-agent
# - transportation-agent
# - public-safety-agent

# Get execution history
curl -X GET http://localhost:8080/decision-engine/history/+1234567890 \
  -H "Authorization: Bearer <token>"

# List all agents
curl -X GET http://localhost:8080/decision-engine/agents \
  -H "Authorization: Bearer <token>"

# Get agent details
curl -X GET http://localhost:8080/decision-engine/agents/qos-optimization-agent \
  -H "Authorization: Bearer <token>"

# Enable/disable agent
curl -X PUT "http://localhost:8080/decision-engine/agents/qos-optimization-agent/enable?enabled=true" \
  -H "Authorization: Bearer <token>"
```

## API Endpoints

### API Gateway (Port 8080)

All requests should go through the API Gateway:

#### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login and get JWT token

#### Connectivity Service
- `POST /connectivity/Qos/sessions` - Retrieve QoS sessions by phone number
- `POST /connectivity/Qos/sessions/create` - Create QoS session
- `GET /connectivity/Qos/sessions/{id}` - Get QoS session by ID
- `POST /connectivity/network-slice/subscriptions` - Create network slice subscription
- `GET /connectivity/network-slice/subscriptions` - Get all network slice subscriptions
- `GET /connectivity/network-slice/subscriptions/{subscriptionId}` - Get network slice subscription by ID
- `DELETE /connectivity/network-slice/subscriptions/{subscriptionId}` - Delete network slice subscription
- `GET /connectivity/health` - Health check

#### Identification Service
- `POST /identification/verify-number` - Verify phone number
- `GET /identification/share-phone-number` - Get device phone number
- `GET /identification/health` - Health check

#### Location Service
- `POST /location/verify` - Verify device location (with optional version parameter)
- `POST /location/verify/v1` - Verify device location (v1 API)
- `POST /location/verify/v2` - Verify device location (v2 API)
- `POST /location/verify/v3` - Verify device location (v3 API)
- `POST /location/retrieve` - Retrieve device location
- `POST /location/geofencing/subscriptions` - Create geofencing subscription
- `GET /location/geofencing/subscriptions` - Get all geofencing subscriptions
- `GET /location/geofencing/subscriptions/{subscriptionId}` - Get geofencing subscription by ID
- `DELETE /location/geofencing/subscriptions/{subscriptionId}` - Delete geofencing subscription
- `GET /location/health` - Health check

#### Device Management Service
- `POST /device/status/connectivity` - Get device connectivity status
- `POST /device/status/roaming` - Get device roaming status
- `GET /device/subscriptions` - Get all device status subscriptions
- `POST /device/subscriptions` - Create device status subscription
- `GET /device/subscriptions/{subscriptionId}` - Get subscription by ID
- `POST /device/swap/retrieve-date` - Retrieve device swap date
- `POST /device/swap/check` - Check device swap
- `GET /device/health` - Health check

#### Nokia NAC Metadata (Shared Module)
- `GET /nokia-nac/metadata/openid-configuration` - Get OpenID configuration metadata
- `GET /nokia-nac/metadata/security.txt` - Get security.txt
- `GET /nokia-nac/metadata/oauth-authorization-server` - Get OAuth authorization server metadata

#### Nokia NAC Authorization (Shared Module)
- `POST /nokia-nac/authorization/token/client-credentials` - Get OAuth2 token using client credentials grant
- `POST /nokia-nac/authorization/token/authorization-code` - Get OAuth2 token using authorization code grant
- `POST /nokia-nac/authorization/token/refresh` - Refresh OAuth2 access token
- `POST /nokia-nac/authorization/token` - Generic token request endpoint
- `GET /nokia-nac/authorization/authorize-url` - Get OAuth2 authorization URL

#### Decision Engine Service
- `POST /decision-engine/execute/{phoneNumber}` - Execute all agents for a device
- `POST /decision-engine/execute/{agentId}/{phoneNumber}` - Execute individual agent
- `GET /decision-engine/history/{phoneNumber}` - Get execution history
- `GET /decision-engine/agents` - List all agents
- `GET /decision-engine/agents/{agentId}` - Get agent details
- `PUT /decision-engine/agents/{agentId}/enable` - Enable/disable agent
- `GET /decision-engine/health` - Health check

#### Monitoring
- `GET /actuator/health` - Health check
- `GET /actuator/metrics` - Metrics endpoint
- `GET /actuator/prometheus` - Prometheus metrics

## OAuth2 Authorization Implementation

The platform implements comprehensive OAuth2 authorization for all Nokia NAC API calls:

### Dynamic Client Credentials
- **Automatic Retrieval**: Client credentials (`client_id` and `client_secret`) are automatically fetched from Nokia NAC authorization server
- **No Manual Configuration**: No need to set environment variables or YAML configuration for credentials
- **Caching**: Credentials are cached after first retrieval to minimize API calls
- **Thread-Safe**: Uses ReentrantLock for concurrent access

### Token Management
- **Automatic Acquisition**: OAuth2 access tokens are automatically acquired when needed
- **Smart Caching**: Tokens are cached with automatic refresh 5 minutes before expiration
- **Bearer Token Authorization**: All Nokia NAC API calls automatically include `Authorization: Bearer {token}` header
- **Multiple Grant Types**: Supports client credentials, authorization code, and refresh token grants

### Implementation Details
- **Token Manager**: `NokiaNacTokenManager` handles token lifecycle management
- **Client Credentials Client**: `NokiaNacClientCredentialsClient` retrieves credentials from API
- **Authorization Client**: `NokiaNacAuthorizationClient` handles OAuth2 token requests
- **All NAC Clients Updated**: 9 Nokia NAC API clients now include OAuth2 Bearer token authorization

### Configuration
Only the RapidAPI key needs to be configured - everything else is automatic:
```yaml
nokia:
  nac:
    rapidapi-key: your-rapidapi-key-here
    authorization-server-url: https://authorization.p-eu.rapidapi.com
    scope: read write
```

## Network APIs Integration

### ✅ Network APIs
- **Quality of Service on Demand (QoD)**: Autonomous QoS optimization
  - Create QoS sessions
  - Retrieve sessions by phone number
  - Get session by ID
- **Network Slice Management**: Dynamic slice allocation

### ✅ Location APIs
- **Location Verification**: Real-time location verification (v1, v2, v3)
  - Verify device location with area definition
  - Support for multiple API versions
- **Location Retrieval**: Get current device location
- **Geofencing**: Geofence monitoring and alerts
- **Population Density**: Analysis via location data

### ✅ Identity & Security APIs
- **Number Verification**: Phone number validation
  - Verify phone number
  - Share/get device phone number
- **SIM Swap Detection**: Automatic detection and response
- **Device Status**: Real-time device health monitoring
  - Connectivity status
  - Roaming status
  - Device status subscriptions

### ✅ Device Management APIs
- **Device Swap**: Device swap detection and management
  - Retrieve device swap date
  - Check device swap status
- **Device Status Subscriptions**: Subscribe to device status updates

### ✅ Metadata APIs
- **OpenID Configuration**: Get OpenID configuration metadata
- **Security.txt**: Get security information
- **OAuth Authorization Server**: Get OAuth authorization server metadata

### ✅ OAuth2 Authorization
- **Dynamic Client Credentials**: Automatically retrieves client_id and client_secret from Nokia NAC
- **Token Management**: Automatic token acquisition, caching, and refresh
- **OAuth2 Grants**: Support for client credentials, authorization code, and refresh token grants
- **Bearer Token Authorization**: All Nokia NAC API calls include OAuth2 Bearer tokens

### ✅ Network Insights
- **Congestion Data**: Network congestion monitoring
- **Device Reachability**: Connectivity and reachability tracking

## Emergency Connectivity Setup

The platform includes comprehensive support for **Guaranteed 5G Connectivity for Emergency Services** with fully implemented services in the shared module.

### Key Features:
- **Emergency Context Detection**: Automatic detection from geofence, SOS button, or external systems
  - `EmergencyContextService`: Detects, manages, and resolves emergency contexts
- **Event-Driven Architecture**: Kafka-based event broadcasting for parallel processing
  - `EmergencyEventProducer`: Publishes emergency events to Kafka
- **Trust & Authorization**: Device identity and SIM integrity validation
  - `TrustValidationService`: Validates device trust, SIM integrity, and calculates trust scores
- **Network State Assessment**: Real-time network condition evaluation
  - `NetworkStateAssessmentService`: Assesses congestion, slice availability, and QoS capacity
- **AI Decision Engine**: Autonomous decision-making with explainability
  - `EmergencyDecisionEngineService`: Evaluates emergencies, applies policies, and generates decisions
- **Network Orchestration**: Guaranteed connectivity execution via Network as Code
  - `NetworkOrchestrationService`: Executes guaranteed connectivity, QoS requests, and slice assignment
- **Continuous Monitoring**: Real-time metrics and automatic remediation
  - `EmergencyMonitoringService`: Monitors metrics, detects degradation, and triggers remediation
- **Audit & Compliance**: Complete audit trail for regulatory compliance
  - `AuditService`: Logs all decisions and actions for compliance

## Use Cases

### 1. Smart Cities (Infrastructure Reporting - CityCare-like)
**Agent**: Smart City Agent  
**Features**:
- Monitors city infrastructure devices (traffic lights, sensors, cameras)
- Automatic QoS boost for critical infrastructure
- Location verification for asset tracking
- Infrastructure health reporting

### 2. Emergency Connectivity
**Agent**: Emergency Connectivity Agent  
**Features**:
- Automatic emergency mode activation
- Maximum QoS priority allocation
- Device reachability verification
- Network congestion monitoring
- **Guaranteed Connectivity**: < 1 second QoS activation for emergency services
- **Trust Validation**: Only verified emergency devices get priority
- **Autonomous Decision Making**: AI-powered emergency response

### 3. Healthcare - Remote Patient Monitoring
**Agent**: Healthcare Monitoring Agent  
**Features**:
- Low-latency QoS for real-time monitoring
- Device status verification for patient safety
- Location verification
- Reliable connectivity guarantees

### 4. Transportation & Event Logistics
**Agent**: Transportation Agent  
**Features**:
- Vehicle/asset location tracking
- Geofencing for logistics
- QoS optimization for moving vehicles
- Device status monitoring

### 5. Public Safety & Sustainability
**Agent**: Public Safety Agent  
**Features**:
- Public safety device monitoring
- Network resource optimization for sustainability
- Population density analysis
- Incident response connectivity

## Configuration

### Application Properties

Each service has its own `application.yaml` with:
- Database configuration (PostgreSQL, MongoDB)
- Kafka configuration
- OAuth2 security settings
- Nokia Network as Code API settings
- Service-specific configurations

### Decision Engine Configuration

```yaml
ai:
  agents:
    enabled: true
    execution-interval: 30  # seconds
    max-concurrent-agents: 10
    decision-engine:
      type: rule-based
      confidence-threshold: 0.7
    data-collection:
      interval: 10  # seconds
      retention-days: 30
```

### Nokia Network as Code Configuration

```yaml
nokia:
  nac:
    base-url: https://network-as-code.p-eu.rapidapi.com
    rapidapi-key: your-rapidapi-key-here
    rapidapi-host: network-as-code.nokia.rapidapi.com
    authorization-server-url: https://authorization.p-eu.rapidapi.com
    scope: read write
    timeout: 30000
    retry-attempts: 3
```

**Important Notes**:
- **Client Credentials**: Automatically retrieved from Nokia NAC authorization server - no manual configuration needed
- **OAuth2 Tokens**: Automatically acquired, cached, and refreshed by `NokiaNacTokenManager`
- **Bearer Token Authorization**: All Nokia NAC API calls automatically include OAuth2 Bearer tokens
- **Token Caching**: Tokens are cached with automatic refresh 5 minutes before expiration

## Security

- **OAuth2 Authorization Server**: Auth Service acts as OAuth2 provider for internal services
- **Nokia NAC OAuth2**: Dynamic client credentials retrieval and automatic token management
- **JWT Tokens**: All services validate JWT tokens for internal API access
- **API Gateway Security**: Centralized authentication and authorization
- **Circuit Breakers**: Resilience4j circuit breakers protect against failures
- **Bearer Token Authorization**: All Nokia NAC API calls secured with OAuth2 Bearer tokens
- **Thread-Safe Token Management**: Token and credential caching with ReentrantLock for concurrent access

## Monitoring

- **Spring Boot Actuator**: Health checks and metrics
- **Prometheus**: Metrics export for monitoring
- **Agent Execution History**: Track agent performance
- **Network Data Collection**: Real-time network metrics

## Circuit Breakers

Resilience4j circuit breakers are configured for all services:
- Sliding window size: 10
- Minimum number of calls: 5
- Failure rate threshold: 50%
- Wait duration in open state: 5 seconds

## Development

### Project Structure

```
5GServicePlatform/
├── apiGateway/              # API Gateway service
├── auth-service/            # OAuth2 Authorization Server
├── connectivityService/     # QoS and Network Slice Management
├── identification-service/  # Number Verification, Device Status
├── locationService/         # Location Verification, Geofencing
├── deviceManagementService/ # SIM Swap, Device Swap
├── decision-engine-service/        # Intelligent Decision Engine Agents
└── shared-module/           # Shared DTOs, utilities, and common services
```

### Shared Module

The `shared-module` contains common components used across all microservices:

- **DTOs**: Common data transfer objects (DeviceDTO, LocationVerificationDto, NokiaNacTokenRequestDTO, etc.)
- **Client Interfaces**: Nokia NAC client interfaces and implementations
  - `NokiaNacAuthorizationClient`: OAuth2 token management
  - `NokiaNacClientCredentialsClient`: Dynamic client credentials retrieval
  - `NokiaNacMetadataClient`: Metadata endpoint access
- **Services**: Shared service interfaces and implementations
  - **OAuth2 Services**: `NokiaNacTokenManager`, `NokiaNacAuthorizationService`
  - **Emergency Services**: `EmergencyContextService`, `TrustValidationService`, `NetworkStateAssessmentService`, `EmergencyDecisionEngineService`, `NetworkOrchestrationService`, `EmergencyMonitoringService`
  - **Internal Communication**: `InternalServiceClient` for service-to-service calls
- **Event Producers**: Kafka event producers (EmergencyEventProducer)
- **Configuration**: Common configurations (Kafka, Security, WebClient, etc.)
- **Utilities**: Common utility classes and annotations
- **Controllers**: Shared REST controllers (NokiaNacMetadataController, NokiaNacAuthorizationController)

### Adding New Agents

1. Create agent class extending `BaseAgent`
2. Implement `doExecute()` method
3. Register agent in `AgentOrchestrationService`
4. Configure agent priority and execution interval

Example:
```java
@Component
public class MyCustomAgent extends BaseAgent {
    public MyCustomAgent() {
        super("my-agent", "My Agent", "Description");
        setPriority(5);
        setExecutionInterval(30);
    }
    
    @Override
    protected AgentResult doExecute(AgentContext context) {
        // Your agent logic here
    }
}
```

### Adding New Nokia API Endpoints

1. Add endpoint configuration to `application.yaml`
2. Add method to `NokiaNacClient` service
3. Use in agents via `InternalServiceClient`

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
- Verify RapidAPI key is correct in `application.yaml`
- Check network connectivity to Nokia NAC endpoints
- Review retry configuration
- Check Nokia API documentation for endpoint changes
- **OAuth2 Token Issues**: 
  - Check logs for "Failed to retrieve client credentials" or "Failed to obtain access token"
  - Verify `authorization-server-url` is correct
  - Ensure RapidAPI key has access to authorization endpoints
  - Check if client credentials are being retrieved successfully (look for "Successfully retrieved client credentials" in logs)
- **403 Forbidden Errors**: 
  - Verify OAuth2 Bearer token is included in requests
  - Check token expiration (tokens auto-refresh 5 minutes before expiration)
  - Verify client credentials are not empty (check logs for credential validation)

### Agent execution issues
- Check agent is enabled: `GET /decision-engine/agents/{agentId}`
- Verify network data collection is working
- Review agent execution logs
- Check decision engine confidence threshold

## Performance

- **Agent Execution**: 10-30 seconds per agent
- **Network Data Collection**: 10 seconds interval
- **API Response Time**: < 500ms average
- **Concurrent Agents**: Up to 10 agents per device

## Future Enhancements

1. **Machine Learning Integration**
   - Train models on historical network data
   - Predict network issues before they occur
   - Optimize agent decision-making

2. **Advanced Coordination**
   - Multi-agent collaboration
   - Conflict resolution
   - Priority-based scheduling

3. **Extended Agent Types**
   - Security/Threat Detection Agent
   - Capacity Planning Agent
   - Cost Optimization Agent

4. **Real-Time Streaming**
   - Kafka integration for real-time events
   - Event-driven agent execution
   - Stream processing for network data

## License

This project is part of the Smart 5G Service Platform.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## Support

For issues and questions:
- Check the troubleshooting section
- Review service logs
- Check API Gateway routes
- Verify agent execution history

---

**Built with Nokia Network as Code APIs**
