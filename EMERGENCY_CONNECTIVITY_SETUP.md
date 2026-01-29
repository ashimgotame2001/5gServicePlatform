# Guaranteed 5G Connectivity for Emergency Services - Setup Guide

## Overview

This document describes the complete setup for implementing **Guaranteed 5G Connectivity for Emergency Services** using Network as Code. The system automatically detects emergencies and guarantees highest-priority 5G connectivity in real-time.

## Architecture Components

### 1. Emergency Context Management (BR-1)
**Location**: `shared-module/src/main/java/com/service/shared/service/EmergencyContextService.java`

**Purpose**: Detects emergency situations in real-time without human intervention.

**Triggers**:
- Device enters emergency geofence
- Emergency button/SOS triggered
- Emergency event from external system (city/hospital/police)

**DTOs**:
- `EmergencyContextDTO` - Emergency context data structure
- `EmergencyEventDTO` - Kafka event for broadcasting

**Key Methods**:
- `detectEmergencyFromGeofence()` - Geofence-based detection
- `detectEmergencyFromSOS()` - SOS button detection
- `detectEmergencyFromExternalEvent()` - External system integration
- `resolveEmergency()` - Emergency resolution
- `cancelEmergency()` - Emergency cancellation

### 2. Event-Driven Backbone (BR-2)
**Location**: `shared-module/src/main/java/com/service/shared/event/EmergencyEventProducer.java`

**Purpose**: Broadcasts emergency events instantly via Kafka for parallel consumption.

**Kafka Topic**: `emergency-events`

**Features**:
- Zero-delay propagation
- Fully decoupled system
- Guaranteed delivery
- Replay support for audits

**Configuration**:
- Producer: `emergencyEventKafkaTemplate` bean
- Consumer: Configured per service via `KafkaConfig`

### 3. Trust & Authorization Validation (BR-3)
**Location**: `shared-module/src/main/java/com/service/shared/service/TrustValidationService.java`

**Purpose**: Validates device identity and prevents abuse of priority APIs.

**Validations**:
- Device identity verification
- SIM/eSIM integrity check
- Emergency role confirmation (ambulance/police/fire)
- Trust score calculation

**DTO**: `TrustValidationDTO`

**Key Methods**:
- `validateDeviceTrust()` - Complete trust validation
- `verifySimIntegrity()` - SIM card verification
- `verifyDeviceIdentity()` - Device identity check
- `calculateTrustScore()` - Trust scoring (0.0 - 1.0)

### 4. Network State Assessment (BR-4)
**Location**: `shared-module/src/main/java/com/service/shared/service/NetworkStateAssessmentService.java`

**Purpose**: Assesses live network conditions before allocating priority.

**Assessments**:
- Congestion level check
- Slice availability check
- QoS capacity check
- Prioritization impact prediction

**DTO**: `NetworkStateDTO`

**Nokia APIs Used**:
- Network status APIs
- Slice availability APIs
- QoS capacity insights

**Key Methods**:
- `assessNetworkState()` - Complete network assessment
- `checkCongestionLevel()` - Congestion analysis
- `checkAvailableSlices()` - Slice availability
- `checkQoSCapacity()` - QoS capacity check
- `predictPrioritizationImpact()` - Impact prediction

### 5. AI Decision Engine (BR-5)
**Location**: `shared-module/src/main/java/com/service/shared/service/EmergencyDecisionEngineService.java`

**Purpose**: Makes autonomous decisions with explainability.

**Decision Logic**:
```
IF emergency = TRUE
AND device_role = AMBULANCE
AND confidence ≥ 0.95
→ APPROVE GUARANTEED CONNECTIVITY
```

**Features**:
- Policy rule evaluation
- Confidence scoring
- Explainable decisions
- Auto-approval logic

**DTO**: `DecisionResultDTO`

**Key Methods**:
- `evaluateEmergency()` - Complete decision evaluation
- `evaluatePolicyRules()` - Policy rule evaluation
- `calculateConfidenceScore()` - Confidence calculation
- `generateDecisionExplanation()` - Decision explanation

### 6. Network Orchestration (BR-6)
**Location**: `shared-module/src/main/java/com/service/shared/service/NetworkOrchestrationService.java`

**Purpose**: Executes guaranteed connectivity via Network as Code APIs.

**Actions**:
- Request QoS on Demand
- Assign priority network slice
- Enable traffic preemption
- Protect emergency traffic end-to-end

**Nokia APIs Used**:
- QoD (Quality on Demand)
- Network Slicing
- Traffic prioritization APIs

**DTO**: `NetworkOrchestrationDTO`

**Key Methods**:
- `executeGuaranteedConnectivity()` - Complete orchestration
- `requestQoSOnDemand()` - QoS activation
- `assignNetworkSlice()` - Slice assignment
- `enableTrafficPreemption()` - Traffic preemption
- `rollbackNetworkChanges()` - Safe rollback

### 7. Continuous Monitoring (BR-7)
**Location**: `shared-module/src/main/java/com/service/shared/service/EmergencyMonitoringService.java`

**Purpose**: Monitors connectivity and dynamically adjusts.

**Monitoring Metrics**:
- Latency (ms)
- Jitter (ms)
- Packet loss (%)
- Throughput (Mbps)
- Connection status
- Health score

**Features**:
- Real-time metrics collection
- Automatic remediation
- Dynamic QoS adjustment
- Traffic rerouting

**DTO**: `MonitoringMetricsDTO`

**Key Methods**:
- `startMonitoring()` - Start monitoring
- `stopMonitoring()` - Stop monitoring
- `getMonitoringMetrics()` - Get current metrics
- `checkRemediationNeeded()` - Check if remediation needed
- `triggerRemediation()` - Trigger automatic remediation

### 8. Audit & Compliance (BR-8)
**Location**: `shared-module/src/main/java/com/service/shared/service/AuditService.java`

**Purpose**: Logs all decisions and actions for compliance.

**Audit Events**:
- Decision logs
- API invocation records
- Network change logs
- Rollback logs

**DTO**: `AuditLogDTO`

**Key Methods**:
- `logAuditEvent()` - General audit logging
- `logDecision()` - Decision logging
- `logApiInvocation()` - API call logging
- `logNetworkChange()` - Network change logging
- `generateComplianceReport()` - Compliance report generation

## Implementation Steps

### Step 1: Database Setup

Create tables for emergency context storage:

```sql
-- Emergency Context Table
CREATE TABLE emergency_context (
    emergency_id VARCHAR(255) PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,
    device_imei VARCHAR(15),
    emergency_type VARCHAR(50) NOT NULL,
    device_role VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    geofence_id VARCHAR(255),
    external_event_id VARCHAR(255),
    detected_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    metadata TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trust Validation Table
CREATE TABLE trust_validation (
    validation_id VARCHAR(255) PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,
    device_imei VARCHAR(15),
    sim_card_number VARCHAR(20),
    status VARCHAR(50) NOT NULL,
    verified_role VARCHAR(50),
    sim_integrity_valid BOOLEAN,
    device_identity_valid BOOLEAN,
    trust_score DOUBLE PRECISION,
    validated_at TIMESTAMP NOT NULL,
    rejection_reason TEXT
);

-- Decision Result Table
CREATE TABLE decision_result (
    decision_id VARCHAR(255) PRIMARY KEY,
    emergency_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    confidence_score DOUBLE PRECISION,
    decided_at TIMESTAMP NOT NULL,
    explanation TEXT,
    recommended_qos_profile VARCHAR(100),
    recommended_slice_id VARCHAR(255),
    risk_score DOUBLE PRECISION,
    metadata TEXT
);

-- Network Orchestration Table
CREATE TABLE network_orchestration (
    orchestration_id VARCHAR(255) PRIMARY KEY,
    emergency_id VARCHAR(255) NOT NULL,
    decision_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    qos_session_id VARCHAR(255),
    slice_id VARCHAR(255),
    executed_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    execution_duration_ms BIGINT,
    error_message TEXT,
    rollback_status VARCHAR(50),
    rolled_back_at TIMESTAMP
);

-- Audit Log Table (MongoDB recommended for high-volume)
-- Or use PostgreSQL with partitioning
CREATE TABLE audit_log (
    audit_id VARCHAR(255) PRIMARY KEY,
    emergency_id VARCHAR(255),
    event_type VARCHAR(50) NOT NULL,
    source_service VARCHAR(100) NOT NULL,
    action VARCHAR(255) NOT NULL,
    result VARCHAR(50) NOT NULL,
    initiated_by VARCHAR(255),
    timestamp TIMESTAMP NOT NULL,
    metadata JSONB
);
```

### Step 2: Kafka Topic Setup

Create Kafka topic for emergency events:

```bash
# Create emergency-events topic
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic emergency-events \
  --partitions 3 \
  --replication-factor 1 \
  --config retention.ms=604800000 \
  --config segment.ms=86400000
```

### Step 3: Service Implementation

Implement services in appropriate microservices:

1. **Emergency Service** (New service or add to existing)
   - Implement `EmergencyContextService`
   - Implement `EmergencyEventProducer` usage
   - Add REST endpoints for emergency detection

2. **Trust Service** (Add to Device Management Service)
   - Implement `TrustValidationService`
   - Integrate with device status APIs

3. **Network Assessment Service** (Add to Connectivity Service)
   - Implement `NetworkStateAssessmentService`
   - Integrate with Nokia Network APIs

4. **Decision Engine** (Add to AI Agent Service)
   - Implement `EmergencyDecisionEngineService`
   - Extend existing decision engine

5. **Network Orchestration** (Add to Connectivity Service)
   - Implement `NetworkOrchestrationService`
   - Integrate with QoS and Slice APIs

6. **Monitoring Service** (New service or add to existing)
   - Implement `EmergencyMonitoringService`
   - Set up metrics collection

7. **Audit Service** (Add to shared module or new service)
   - Implement `AuditService`
   - Set up MongoDB for audit logs

### Step 4: Configuration

Add to `application.yaml`:

```yaml
emergency:
  connectivity:
    # Emergency detection settings
    detection:
      geofence-enabled: true
      sos-enabled: true
      external-events-enabled: true
    
    # Trust validation settings
    trust:
      min-trust-score: 0.8
      sim-integrity-required: true
      device-identity-required: true
    
    # Decision engine settings
    decision:
      min-confidence-score: 0.95
      auto-approval-enabled: true
      manual-review-threshold: 0.85
    
    # Network orchestration settings
    orchestration:
      qos-activation-timeout-ms: 1000
      slice-assignment-timeout-ms: 2000
      rollback-on-failure: true
    
    # Monitoring settings
    monitoring:
      metrics-interval-seconds: 5
      remediation-threshold-latency-ms: 100
      remediation-threshold-packet-loss: 5.0
      auto-remediation-enabled: true
    
    # Audit settings
    audit:
      log-all-decisions: true
      log-all-api-calls: true
      retention-days: 365
```

### Step 5: Kafka Consumer Setup

Add Kafka consumers in services that need to react to emergency events:

```java
@Component
@RequiredArgsConstructor
public class EmergencyEventConsumer {
    
    @KafkaListener(topics = "emergency-events", groupId = "trust-service-group")
    public void handleEmergencyEvent(EmergencyEventDTO event) {
        // Handle emergency event
        // Trigger trust validation, network assessment, etc.
    }
}
```

## Testing Checklist

- [ ] Emergency detection from geofence
- [ ] Emergency detection from SOS button
- [ ] Emergency detection from external system
- [ ] Kafka event publishing and consumption
- [ ] Trust validation for trusted devices
- [ ] Trust validation rejection for untrusted devices
- [ ] Network state assessment
- [ ] Decision engine approval flow
- [ ] Decision engine denial flow
- [ ] QoS activation (< 1 second)
- [ ] Network slice assignment
- [ ] Traffic preemption
- [ ] Monitoring metrics collection
- [ ] Automatic remediation
- [ ] Emergency resolution
- [ ] Network rollback
- [ ] Audit log generation
- [ ] Compliance report generation

## Performance Targets

- Emergency detection: < 100ms
- Event broadcasting: < 50ms
- Trust validation: < 200ms
- Network assessment: < 500ms
- Decision evaluation: < 300ms
- QoS activation: < 1000ms
- Monitoring interval: 5 seconds
- Audit log write: < 50ms

## Security Considerations

1. **Trust Validation**: All devices must pass trust validation before priority allocation
2. **Rate Limiting**: Prevent abuse of emergency APIs
3. **Audit Trail**: All actions must be logged for compliance
4. **Role-Based Access**: Only authorized roles can trigger emergencies
5. **SIM Integrity**: Verify SIM/eSIM integrity to prevent spoofing

## Compliance & SLA

- **SLA**: 99.9% uptime for emergency connectivity
- **Response Time**: < 1 second for QoS activation
- **Audit Retention**: 365 days minimum
- **Decision Explainability**: All decisions must be explainable
- **Rollback Capability**: All network changes must be reversible

## Next Steps

1. Implement service interfaces in appropriate microservices
2. Create REST controllers for emergency endpoints
3. Set up database tables
4. Configure Kafka topics
5. Implement Kafka consumers
6. Add monitoring and alerting
7. Create integration tests
8. Set up CI/CD pipeline
9. Deploy to staging environment
10. Performance testing
11. Security audit
12. Production deployment

## Support & Documentation

- DTOs: `shared-module/src/main/java/com/service/shared/dto/`
- Services: `shared-module/src/main/java/com/service/shared/service/`
- Events: `shared-module/src/main/java/com/service/shared/event/`
- Configuration: `shared-module/src/main/java/com/service/shared/config/`
