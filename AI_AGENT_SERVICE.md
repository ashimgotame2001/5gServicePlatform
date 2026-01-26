# AI Agent Service - Intelligent Autonomous Agents for 5G Network

## Overview

The AI Agent Service is a microservice that provides intelligent, autonomous AI agents that use real telecom network data and APIs to solve problems autonomously on a real 5G network.

## Architecture

### Core Components

1. **Agent Framework**
   - `Agent` interface: Base contract for all agents
   - `BaseAgent`: Abstract base class with common functionality
   - `AgentContext`: Context object with network data and state
   - `AgentResult`: Result of agent execution
   - `AgentAction`: Represents actions taken by agents

2. **Specialized Agents**
   - **QoS Optimization Agent**: Autonomously optimizes Quality of Service based on real-time network conditions
   - **Network Monitoring Agent**: Continuously monitors network conditions and detects anomalies
   - **Location Verification Agent**: Autonomously verifies and manages location data
   - **Device Management Agent**: Autonomously manages device and SIM card operations
   - **Smart City Agent**: Monitors city infrastructure (traffic lights, sensors, cameras) - CityCare-like
   - **Emergency Connectivity Agent**: Guarantees connectivity for emergency services
   - **Healthcare Monitoring Agent**: Ensures reliable connectivity for remote patient monitoring
   - **Transportation Agent**: Manages connectivity for transportation and event logistics
   - **Public Safety Agent**: Monitors public safety systems and sustainability

3. **Services**
   - **NetworkDataCollectionService**: Collects real-time network data from Nokia APIs and internal services
   - **DecisionEngine**: Makes autonomous decisions based on network data using rule-based logic
   - **AgentOrchestrationService**: Orchestrates and coordinates multiple agents
   - **InternalServiceClient**: Client for internal service-to-service communication

4. **Data Models**
   - `NetworkData`: Comprehensive network data structure including:
     - Connectivity metrics (signal strength, latency, throughput)
     - Location data (latitude, longitude, accuracy)
     - Device status (IMEI, SIM card, active status)
     - QoS metrics (profile, priority, bandwidth)

## Features

### Autonomous Problem Solving
- Agents analyze real network data from Nokia APIs
- Make decisions autonomously based on network conditions
- Execute actions through existing microservices
- Learn from outcomes and adapt behavior

### Real-Time Network Data
- Collects data from Nokia Network as Code APIs (RapidAPI)
- Integrates with internal services (Connectivity, Identification, Location, Device Management)
- Caches data for performance
- Monitors network conditions continuously

### Intelligent Decision Making
- Rule-based decision engine with configurable confidence thresholds
- Analyzes multiple network metrics simultaneously
- Prioritizes actions based on severity
- Provides recommendations and insights

### Agent Orchestration
- Coordinates multiple agents
- Manages agent execution lifecycle
- Tracks execution history
- Supports concurrent agent execution

## API Endpoints

### Agent Execution
- `POST /ai-agents/execute/{phoneNumber}` - Execute all agents for a device
- `GET /ai-agents/history/{phoneNumber}` - Get execution history for a device

### Agent Management
- `GET /ai-agents/agents` - Get all registered agents
- `GET /ai-agents/agents/{agentId}` - Get agent details
- `PUT /ai-agents/agents/{agentId}/enable?enabled={true|false}` - Enable/disable agent

### Health & Monitoring
- `GET /ai-agents/health` - Health check with agent statistics

## Configuration

### Application Properties

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

### Agent Configuration

Each agent can be configured with:
- Priority (higher = more important)
- Execution interval (how often to run)
- Enabled/disabled state

## Usage Examples

### Execute Agents for a Device

```bash
curl -X POST http://localhost:8080/ai-agents/execute/+1234567890 \
  -H "Authorization: Bearer <token>"
```

### Get Agent Execution History

```bash
curl -X GET http://localhost:8080/ai-agents/history/+1234567890 \
  -H "Authorization: Bearer <token>"
```

### List All Agents

```bash
curl -X GET http://localhost:8080/ai-agents/agents \
  -H "Authorization: Bearer <token>"
```

## Agent Types

### 1. QoS Optimization Agent
- **Purpose**: Autonomously optimizes Quality of Service
- **Priority**: 8 (High)
- **Execution Interval**: 30 seconds
- **Actions**:
  - Adjusts QoS priority based on signal strength
  - Increases bandwidth for low throughput scenarios
  - Reduces latency for high-latency connections

### 2. Network Monitoring Agent
- **Purpose**: Monitors network conditions and detects anomalies
- **Priority**: 7 (High)
- **Execution Interval**: 10 seconds
- **Capabilities**:
  - Detects low signal strength
  - Identifies high latency
  - Monitors throughput
  - Tracks device connectivity status

### 3. Location Verification Agent
- **Purpose**: Verifies and manages location data
- **Priority**: 6 (Medium)
- **Execution Interval**: 60 seconds
- **Actions**:
  - Verifies location when data is stale
  - Improves location accuracy
  - Monitors location data freshness

### 4. Device Management Agent
- **Purpose**: Manages device and SIM card operations
- **Priority**: 5 (Medium)
- **Execution Interval**: 120 seconds
- **Actions**:
  - Swaps devices when inactive
  - Swaps SIM cards when needed
  - Monitors device health

### 5. Smart City Agent
- **Purpose**: Monitors city infrastructure (traffic lights, sensors, cameras)
- **Priority**: 9 (Very High)
- **Execution Interval**: 20 seconds
- **Actions**:
  - Monitors infrastructure device connectivity
  - Requests QoS boost for critical infrastructure
  - Verifies device locations
  - Generates infrastructure health reports

### 6. Emergency Connectivity Agent
- **Purpose**: Guarantees connectivity for emergency services
- **Priority**: 10 (Highest)
- **Execution Interval**: 10 seconds
- **Actions**:
  - Activates emergency QoS mode
  - Ensures maximum priority and bandwidth
  - Verifies device reachability
  - Monitors network congestion

### 7. Healthcare Monitoring Agent
- **Purpose**: Ensures reliable connectivity for remote patient monitoring
- **Priority**: 9 (Very High)
- **Execution Interval**: 15 seconds
- **Actions**:
  - Optimizes QoS for low-latency healthcare monitoring
  - Verifies device status for patient safety
  - Monitors device location
  - Ensures reliable connectivity

### 8. Transportation Agent
- **Purpose**: Manages connectivity for transportation and event logistics
- **Priority**: 7 (High)
- **Execution Interval**: 30 seconds
- **Actions**:
  - Monitors vehicle/asset locations via geofencing
  - Optimizes QoS for moving vehicles
  - Manages fleet device connectivity
  - Tracks assets in real-time

### 9. Public Safety Agent
- **Purpose**: Monitors public safety systems and sustainability
- **Priority**: 8 (High)
- **Execution Interval**: 20 seconds
- **Actions**:
  - Monitors public safety devices
  - Optimizes network resources for sustainability
  - Analyzes population density via location data
  - Ensures connectivity during incidents

## Decision Engine

The decision engine uses rule-based logic to make autonomous decisions:

### QoS Analysis Rules
- Low signal strength (< 50) → Increase QoS priority
- High latency (> 100ms) → Adjust QoS parameters
- Low throughput (< 10 Mbps) → Increase bandwidth
- Default QoS profile → Upgrade to optimized profile

### Location Analysis Rules
- Stale location data (maxAge > 120s) → Verify location
- Low accuracy (> 100m) → Request new location

### Device Analysis Rules
- Inactive device → Consider device swap
- Error status → Investigate and swap if needed

## Integration

### With Nokia Network as Code APIs
- Location Retrieval API
- Number Verification API
- Device Status API
- QoS Management API

### With Internal Services
- Connectivity Service (QoS management)
- Identification Service (Device status)
- Location Service (Location verification)
- Device Management Service (Device/SIM swaps)

## Monitoring & Observability

- Spring Boot Actuator endpoints
- Prometheus metrics
- Execution history tracking
- Agent performance metrics
- Decision confidence scores

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

## Port

The AI Agent Service runs on port **8086**.

## Dependencies

- Spring Boot 4.0.2
- Spring WebFlux (WebClient)
- Spring Security + OAuth2 Resource Server
- Spring Data JPA (PostgreSQL)
- Spring Data MongoDB
- Spring Kafka
- Lombok

## Security

- OAuth2 Resource Server with JWT validation
- All endpoints require authentication (except `/health`)
- Integrates with Auth Service for token validation
