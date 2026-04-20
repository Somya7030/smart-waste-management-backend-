# 🗑️ Smart AI + IoT Waste Management System
### Production-Ready Spring Boot Backend | Hackathon Edition

---

## 📐 System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                     SMART WASTE MANAGEMENT SYSTEM                   │
├──────────────┬──────────────────────────────┬───────────────────────┤
│  IoT LAYER   │       BACKEND LAYER          │    DATA / OUTPUT      │
│              │                              │                       │
│  [Sensors]   │  ┌─────────────────────┐     │  ┌────────────────┐   │
│  Fill Level  │  │  Spring Boot App    │     │  │   H2 / MySQL   │   │
│  Ultrasonic  │  │  Port: 8080         │     │  │   JPA/Hibernate│   │
│  Detectors   │  └────────┬────────────┘     │  └────────────────┘   │
│      │       │           │                  │                       │
│      ▼       │  ┌────────▼────────────┐     │  ┌────────────────┐   │
│  [MQTT]      │  │   REST Controllers  │     │  │  /uploads dir  │   │
│  Broker      │  │   BinController     │     │  │  Images stored │   │
│  HiveMQ      │  │   AlertController   │     │  └────────────────┘   │
│  Topic:      │  │   DashboardCtrl     │     │                       │
│  bins/data   │  │   MapController     │     │  ┌────────────────┐   │
│      │       │  │   RouteController   │     │  │  REST Clients  │   │
│      ▼       │  │   ReportController  │     │  │  Dashboard UI  │   │
│  [MQTT       │  │   ClassifyController│     │  │  Mobile App    │   │
│  Handler]    │  └────────┬────────────┘     │  └────────────────┘   │
│  Parses      │           │                  │                       │
│  payload     │  ┌────────▼────────────┐     │                       │
│  binId,fill  │  │    Service Layer    │     │                       │
│      │       │  │  BinService         │     │                       │
│      ▼       │  │  AlertService       │     │                       │
│  [BinService]│  │  DashboardService   │     │                       │
│  Update DB   │  │  RouteOptimization  │     │                       │
│  + Alert     │  │  ClassifyService    │     │                       │
│              │  │  CitizenRptService  │     │                       │
└──────────────┘  └─────────────────────┘     └───────────────────────┘
```

---

## 🧠 ML Route Optimization — Algorithm Design

```
INPUT: All bins with fillLevel, lat, lon, lastUpdatedTime
         │
         ▼
┌─────────────────────────────────────┐
│  STEP 1: FEATURE ENGINEERING        │
│                                     │
│  priority = (fillLevel × 0.7)       │
│           + (50.0 if FULL)          │
│           + (min(hoursOld × 0.5,20))│
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│  STEP 2: KMeans++ CLUSTERING        │
│                                     │
│  k = ceil(bins / 5)                 │
│  • Init: spread-out seeds (KM++)    │
│  • Assign bins to nearest centroid  │
│  • Recompute centroids (avg lat/lon)│
│  • Repeat until convergence         │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│  STEP 3: CLUSTER SORTING            │
│                                     │
│  Sort clusters by dist from depot   │
│  Sort bins within cluster by        │
│  priority DESC (FULL + high fill    │
│  bins visited first)                │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│  STEP 4: ROUTE SEQUENCING           │
│                                     │
│  Depot → Zone-A → Zone-B → Return   │
│  Haversine distance computed        │
│  Estimated time = travel + 5min/bin │
└─────────────────────────────────────┘
```

---

## 📁 Project Structure

```
smart-waste-management/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/smartwaste/
    │   │   ├── SmartWasteManagementApplication.java
    │   │   ├── config/
    │   │   │   ├── AppProperties.java
    │   │   │   ├── MqttConfig.java
    │   │   │   └── WebConfig.java
    │   │   ├── controller/
    │   │   │   ├── AlertController.java
    │   │   │   ├── BinController.java
    │   │   │   ├── CitizenReportController.java
    │   │   │   ├── DashboardController.java
    │   │   │   ├── IoTSimulatorController.java
    │   │   │   ├── MapController.java
    │   │   │   ├── RouteOptimizationController.java
    │   │   │   └── WasteClassificationController.java
    │   │   ├── dto/
    │   │   │   ├── AlertDTO.java
    │   │   │   ├── ApiResponse.java
    │   │   │   ├── BinDTO.java
    │   │   │   ├── BinUpdateRequest.java
    │   │   │   ├── CitizenReportDTO.java
    │   │   │   ├── DashboardStatsDTO.java
    │   │   │   ├── MapBinDTO.java
    │   │   │   ├── RouteOptimizationRequest.java
    │   │   │   ├── RouteOptimizationResponse.java
    │   │   │   └── WasteClassificationDTO.java
    │   │   ├── exception/
    │   │   │   ├── FileStorageException.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   ├── init/
    │   │   │   └── DataInitializer.java
    │   │   ├── model/
    │   │   │   ├── Alert.java
    │   │   │   ├── AlertSeverity.java
    │   │   │   ├── Bin.java
    │   │   │   ├── BinStatus.java
    │   │   │   ├── CitizenReport.java
    │   │   │   ├── ReportStatus.java
    │   │   │   └── WasteClassification.java
    │   │   ├── mqtt/
    │   │   │   ├── MqttMessageHandler.java
    │   │   │   └── MqttPublisher.java
    │   │   ├── repository/
    │   │   │   ├── AlertRepository.java
    │   │   │   ├── BinRepository.java
    │   │   │   ├── CitizenReportRepository.java
    │   │   │   └── WasteClassificationRepository.java
    │   │   ├── service/
    │   │   │   ├── AlertService.java
    │   │   │   ├── BinService.java
    │   │   │   ├── CitizenReportService.java
    │   │   │   ├── DashboardService.java
    │   │   │   ├── RouteOptimizationService.java
    │   │   │   └── WasteClassificationService.java
    │   │   └── util/
    │   │       └── GeoUtils.java
    │   └── resources/
    │       └── application.yml
    └── test/
        ├── java/com/smartwaste/
        │   └── SmartWasteManagementApplicationTests.java
        └── resources/
            └── application.yml
```

---

## 🔌 Complete API Reference

### 1. BIN MANAGEMENT

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/bins` | Get all bins (optional `?status=FULL`) |
| GET | `/api/bins/{id}` | Get bin by ID |
| GET | `/api/bins/code/{binCode}` | Get bin by code |
| POST | `/api/bins/update` | Update bin fill level |

**POST /api/bins/update — Request:**
```json
{
  "binCode": "BIN-001",
  "fillLevel": 85.0,
  "latitude": 12.9716,
  "longitude": 77.5946,
  "location": "MG Road Zone A"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Bin updated successfully",
  "data": {
    "id": 1,
    "binCode": "BIN-001",
    "location": "MG Road Zone A",
    "fillLevel": 85.0,
    "status": "FULL",
    "latitude": 12.9716,
    "longitude": 77.5946,
    "lastUpdatedTime": "2024-01-15T10:30:00"
  }
}
```

---

### 2. ALERTS

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/alerts` | Get all active (unresolved) alerts |
| GET | `/api/alerts/all` | Get full alert history |
| PATCH | `/api/alerts/{id}/resolve` | Resolve an alert |

**GET /api/alerts — Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "binId": 1,
      "binCode": "BIN-001",
      "binLocation": "MG Road Zone A",
      "message": "Bin BIN-001 requires immediate collection (92.0% full).",
      "severity": "CRITICAL",
      "fillLevelAtAlert": 92.0,
      "createdAt": "2024-01-15T10:30:00",
      "resolved": false,
      "latitude": 12.9758,
      "longitude": 77.6085
    }
  ]
}
```

---

### 3. DASHBOARD STATS

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/stats` | Get aggregate statistics |

**GET /api/stats — Response:**
```json
{
  "success": true,
  "data": {
    "totalBins": 20,
    "fullBins": 8,
    "halfBins": 7,
    "emptyBins": 5,
    "activeAlerts": 8,
    "pendingReports": 2,
    "averageFillLevel": 63.5,
    "collectionEfficiency": 60.0
  }
}
```

---

### 4. MAP DATA

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/map/bins` | Get all bins with coordinates for map |

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "binId": 1,
      "binCode": "BIN-001",
      "latitude": 12.9758,
      "longitude": 77.6085,
      "status": "FULL",
      "fillLevel": 92.0,
      "location": "MG Road, Zone A"
    }
  ]
}
```

---

### 5. ROUTE OPTIMIZATION (ML-Powered)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/route/optimize` | Run ML route optimization |

**Request:**
```json
{
  "depotLatitude": 12.9716,
  "depotLongitude": 77.5946,
  "clusterRadiusKm": 2.0,
  "fullBinsOnly": false,
  "maxBinsPerRoute": 20
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "clusters": [
      {
        "clusterIndex": 1,
        "clusterLabel": "Zone-A",
        "stops": [
          {
            "stopOrder": 1,
            "binId": 11,
            "binCode": "BIN-011",
            "location": "Rajajinagar West",
            "latitude": 12.9897,
            "longitude": 77.5538,
            "fillLevel": 95.0,
            "status": "FULL",
            "priority": 116.5,
            "stopType": "BIN"
          }
        ],
        "clusterCenterLat": 12.989700,
        "clusterCenterLon": 77.553800,
        "clusterPriority": 116.5
      }
    ],
    "totalBins": 15,
    "totalDistance": 42.3,
    "estimatedTime": "2h 0min",
    "routeSummary": "Optimized route: 15 bins across 3 zones | Distance: 42.3 km | ETA: 2h 0min"
  }
}
```

---

### 6. CITIZEN REPORTS

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/reports` | Submit report with image (multipart) |
| GET | `/api/reports` | Get all reports |
| GET | `/api/reports/{id}` | Get single report |
| PATCH | `/api/reports/{id}/status?status=RESOLVED` | Update status |
| GET | `/api/reports/image/{fileName}` | Serve image file |

**POST /api/reports — Form Data:**
```
image         : [file]
location      : "Koramangala 5th Block"
description   : "Overflowing bin near park entrance"
latitude      : 12.9352   (optional)
longitude     : 77.6245   (optional)
reporterContact: "9876543210" (optional)
```

---

### 7. AI WASTE CLASSIFICATION

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/classify` | Classify waste from image |
| GET | `/api/classify/image/{fileName}` | Serve classified image |

**POST /api/classify — Form Data:**
```
image : [file] (jpg/png/webp)
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "wasteType": "Plastic",
    "confidence": 0.92,
    "confidencePercent": "92%",
    "disposalInstructions": "Place in blue recycling bin. Rinse before disposal.",
    "recyclable": "Yes - Check resin code",
    "classifiedAt": "2024-01-15T10:30:00"
  }
}
```

---

### 8. MQTT / IoT SIMULATOR

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/simulate/bin` | Simulate single IoT sensor update |
| POST | `/api/simulate/bulk` | Simulate multiple sensors |

**POST /api/simulate/bin:**
```json
{ "binCode": "BIN-001", "fillLevel": 87.5 }
```

**POST /api/simulate/bulk:**
```json
[
  { "binCode": "BIN-001", "fillLevel": 90.0 },
  { "binCode": "BIN-002", "fillLevel": 45.0 },
  { "binCode": "BIN-003", "fillLevel": 15.0 }
]
```

---

### 9. H2 DATABASE CONSOLE
```
URL  : http://localhost:8080/h2-console
JDBC : jdbc:h2:mem:wastedb
User : sa
Pass : (empty)
```

---

## 🚀 How to Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Option A — H2 In-Memory (Zero Config, Start Immediately)

```bash
# Clone / unzip project
cd smart-waste-management

# Build
mvn clean package -DskipTests

# Run
java -jar target/smart-waste-management-1.0.0.jar
```

App starts at: **http://localhost:8080**
20 bins auto-seeded with varied fill levels.

---

### Option B — MySQL

```bash
# Create DB
mysql -u root -p -e "CREATE DATABASE smart_waste_db;"

# Edit application.yml or pass via args:
java -jar target/smart-waste-management-1.0.0.jar \
  --spring.profiles.active=mysql \
  --spring.datasource.username=root \
  --spring.datasource.password=yourpassword
```

---

### Option C — Docker

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/smart-waste-management-1.0.0.jar app.jar
RUN mkdir -p /app/uploads
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

```bash
docker build -t smart-waste .
docker run -p 8080:8080 smart-waste
```

---

## 🧪 Quick Demo Walkthrough

```bash
BASE=http://localhost:8080

# 1. View dashboard
curl $BASE/api/stats

# 2. View all bins on map
curl $BASE/api/map/bins

# 3. View active alerts
curl $BASE/api/alerts

# 4. Update a bin via REST (simulating IoT)
curl -X POST $BASE/api/bins/update \
  -H "Content-Type: application/json" \
  -d '{"binCode":"BIN-005","fillLevel":95.0}'

# 5. Run ML route optimization
curl -X POST $BASE/api/route/optimize \
  -H "Content-Type: application/json" \
  -d '{"depotLatitude":12.9716,"depotLongitude":77.5946}'

# 6. Simulate IoT data via MQTT
curl -X POST $BASE/api/simulate/bin \
  -H "Content-Type: application/json" \
  -d '{"binCode":"BIN-010","fillLevel":88.0}'

# 7. Classify waste (requires an image file)
curl -X POST $BASE/api/classify \
  -F "image=@/path/to/plastic_bottle.jpg"

# 8. Submit citizen report
curl -X POST $BASE/api/reports \
  -F "image=@/path/to/photo.jpg" \
  -F "location=Koramangala 5th Block" \
  -F "description=Overflowing bin" \
  -F "latitude=12.9352" \
  -F "longitude=77.6245"
```

---

## 📋 Status Logic

| Fill Level | Status | Action |
|------------|--------|--------|
| < 40% | `EMPTY` | No action needed |
| 40–80% | `HALF` | Monitor |
| > 80% | `FULL` | ⚠️ Alert triggered — collect immediately |
| > 90% | `FULL` + `CRITICAL` | 🔴 Critical alert |

---

## 🔧 MQTT Integration

- **Broker**: HiveMQ Public (configurable in `application.yml`)
- **Topic**: `bins/data`
- **Payload format**: `binCode,fillLevel`
- **Example**: `BIN-007,87.5`

The system auto-reconnects on broker disconnect. Use `/api/simulate/bin` to test without a physical MQTT broker.

---

## 🏗️ Design Principles Applied

- **SOLID** — Single responsibility per service, open for extension
- **DTO Pattern** — No entity leakage to API consumers
- **Clean Architecture** — Controller → Service → Repository layers
- **Global Exception Handling** — Consistent error responses
- **Validation** — Jakarta Bean Validation on all inputs
- **Logging** — SLF4J with structured log messages throughout
- **Idempotent updates** — MQTT messages safely re-processed
- **Auto-create bins** — Unknown bin codes auto-registered on first IoT message
