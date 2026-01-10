# GeoServices Microservice - Implementierungs-Playbook

## 🎯 Ziel
Extraktion von GeoServices aus OFBiz als eigenständigen Microservice mit REST-API und eigener Datenbank.

---

## 📊 Projekt-Übersicht

| Aspekt | Details |
|--------|---------|
| **Service** | GeoServices |
| **Komplexität** | 🟢 Niedrig |
| **Abhängigkeiten** | 12 (alle zu Framework/Utilities) |
| **Eingehende Deps** | 0 (niemand hängt davon ab) |
| **Geschätzte Dauer** | 5 Wochen |
| **Team-Größe** | 2-3 Entwickler |
| **Risiko** | 🟢 Niedrig |

---

## 🏗️ Architektur-Design

### Aktuelle Situation (Monolith)

```
┌─────────────────────────────────────┐
│         OFBiz Monolith              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   GeoServices               │   │
│  │                             │   │
│  │  - calculateDistance()      │   │
│  │  - getGeoPoint()            │   │
│  │  - validateGeoData()        │   │
│  │  - ... weitere Methoden     │   │
│  │                             │   │
│  │  Abhängigkeiten:            │   │
│  │  - DispatchContext          │   │
│  │  - GeoWorker                │   │
│  │  - UtilMisc                 │   │
│  └─────────────────────────────┘   │
│                                     │
│  Datenbank: OFBiz-DB               │
│  (Geo-Tabellen gemischt)           │
│                                     │
└─────────────────────────────────────┘
```

### Zielzustand (Microservice)

```
┌──────────────────────────────────────────────────────────┐
│                                                          │
│  ┌─────────────────────────────────────────────────┐   │
│  │         OFBiz Monolith (reduziert)              │   │
│  │                                                 │   │
│  │  - Alle anderen Services                        │   │
│  │  - GeoServices REST-Client                      │   │
│  │                                                 │   │
│  │  Datenbank: OFBiz-DB (ohne Geo-Tabellen)       │   │
│  └─────────────────────────────────────────────────┘   │
│                                                          │
│                         ↕ REST-API                       │
│                                                          │
│  ┌─────────────────────────────────────────────────┐   │
│  │      GeoServices Microservice (Docker)          │   │
│  │                                                 │   │
│  │  ┌───────────────────────────────────────────┐ │   │
│  │  │  Spring Boot Application                  │ │   │
│  │  │                                           │ │   │
│  │  │  - GeoController (REST-API)               │ │   │
│  │  │  - GeoService (Business Logic)            │ │   │
│  │  │  - GeoRepository (Data Access)            │ │   │
│  │  │  - GeoWorker (Helper)                     │ │   │
│  │  │                                           │ │   │
│  │  │  Abhängigkeiten:                          │ │   │
│  │  │  - Spring Framework                       │ │   │
│  │  │  - Spring Data JPA                        │ │   │
│  │  │  - PostgreSQL Driver                      │ │   │
│  │  └───────────────────────────────────────────┘ │   │
│  │                                                 │   │
│  │  Datenbank: Geo-DB (PostgreSQL)               │   │
│  │  (Nur Geo-Tabellen)                           │   │
│  └─────────────────────────────────────────────────┘   │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

## 📋 Detaillierte Implementierungs-Schritte

### Phase 1: Analyse & Planung (Woche 1)

#### Schritt 1.1: GeoServices-Methoden dokumentieren

```bash
# Finden Sie alle öffentlichen Methoden in GeoServices
grep -n "public static" framework/catalina/ofbiz-component.xml
# oder in der Quelle:
find . -name "GeoServices.java" -exec grep "public static" {} \;
```

**Zu dokumentierende Methoden:**
- [ ] calculateDistance()
- [ ] getGeoPoint()
- [ ] validateGeoData()
- [ ] ... (alle weiteren)

#### Schritt 1.2: Aufrufer identifizieren

```cypher
# Neo4j Query: Wer ruft GeoServices auf?
MATCH (caller)-[:DEPENDS_ON]->(s:Type {name: 'GeoServices'})
RETURN DISTINCT caller.name as caller
```

**Erwartetes Ergebnis:** 0 Aufrufer (da 0 eingehende Abhängigkeiten)

#### Schritt 1.3: Datenmodell analysieren

```sql
-- Finden Sie alle Geo-bezogenen Tabellen
SELECT table_name FROM information_schema.tables 
WHERE table_name LIKE '%geo%' OR table_name LIKE '%GEO%';
```

**Zu migrierende Tabellen:**
- [ ] GEO_POINT
- [ ] GEO_REGION
- [ ] ... (alle Geo-Tabellen)

#### Schritt 1.4: REST-API-Spezifikation erstellen

```yaml
# geo-service-api.yaml (OpenAPI 3.0)
openapi: 3.0.0
info:
  title: Geo Service API
  version: 1.0.0
  description: Microservice für geografische Daten und Berechnungen

servers:
  - url: http://localhost:8081
    description: Development
  - url: https://geo-service.example.com
    description: Production

paths:
  /api/v1/geo/distance:
    post:
      summary: Berechne Entfernung zwischen zwei Punkten
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                lat1:
                  type: number
                lon1:
                  type: number
                lat2:
                  type: number
                lon2:
                  type: number
      responses:
        '200':
          description: Erfolgreiche Berechnung
          content:
            application/json:
              schema:
                type: object
                properties:
                  distance:
                    type: number
                  unit:
                    type: string
                    enum: [km, miles]

  /api/v1/geo/point/{id}:
    get:
      summary: Rufe einen Geo-Punkt ab
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Geo-Punkt gefunden
        '404':
          description: Geo-Punkt nicht gefunden
```

---

### Phase 2: Vorbereitung (Woche 2)

#### Schritt 2.1: Spring Boot Projekt erstellen

```bash
# Verwenden Sie Spring Boot Starter
mkdir geo-service
cd geo-service

# Erstellen Sie pom.xml
cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.apache.ofbiz.geo</groupId>
    <artifactId>geo-service</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>Geo Service</name>
    <description>Microservice für geografische Daten</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.0.0</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>11</java.version>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.5.0</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Testcontainers -->
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <version>1.17.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <version>1.17.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
EOF
```

#### Schritt 2.2: Projektstruktur erstellen

```
geo-service/
├── src/
│   ├── main/
│   │   ├── java/org/apache/ofbiz/geo/
│   │   │   ├── GeoServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   └── GeoController.java
│   │   │   ├── service/
│   │   │   │   ├── GeoService.java
│   │   │   │   └── GeoServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   ├── GeoPointRepository.java
│   │   │   │   └── GeoRegionRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── GeoPoint.java
│   │   │   │   └── GeoRegion.java
│   │   │   ├── dto/
│   │   │   │   ├── DistanceRequest.java
│   │   │   │   ├── DistanceResponse.java
│   │   │   │   └── GeoPointDTO.java
│   │   │   ├── exception/
│   │   │   │   └── GeoServiceException.java
│   │   │   └── util/
│   │   │       └── GeoWorker.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   │           └── V1__initial_schema.sql
│   └── test/
│       └── java/org/apache/ofbiz/geo/
│           ├── GeoServiceApplicationTests.java
│           ├── service/
│           │   └── GeoServiceImplTest.java
│           └── controller/
│               └── GeoControllerTest.java
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

#### Schritt 2.3: GeoServices extrahieren

```java
// src/main/java/org/apache/ofbiz/geo/service/GeoService.java
package org.apache.ofbiz.geo.service;

import java.util.Map;

public interface GeoService {
    
    /**
     * Berechne die Entfernung zwischen zwei geografischen Punkten
     */
    double calculateDistance(double lat1, double lon1, double lat2, double lon2);
    
    /**
     * Rufe einen Geo-Punkt ab
     */
    Map<String, Object> getGeoPoint(String geoPointId);
    
    /**
     * Validiere geografische Daten
     */
    boolean validateGeoData(Map<String, Object> geoData);
    
    // ... weitere Methoden
}
```

```java
// src/main/java/org/apache/ofbiz/geo/service/GeoServiceImpl.java
package org.apache.ofbiz.geo.service;

import org.apache.ofbiz.geo.entity.GeoPoint;
import org.apache.ofbiz.geo.repository.GeoPointRepository;
import org.apache.ofbiz.geo.util.GeoWorker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class GeoServiceImpl implements GeoService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeoServiceImpl.class);
    
    private final GeoPointRepository geoPointRepository;
    private final GeoWorker geoWorker;
    
    public GeoServiceImpl(GeoPointRepository geoPointRepository, GeoWorker geoWorker) {
        this.geoPointRepository = geoPointRepository;
        this.geoWorker = geoWorker;
    }
    
    @Override
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        logger.info("Calculating distance from ({},{}) to ({},{})", lat1, lon1, lat2, lon2);
        return geoWorker.calculateDistance(lat1, lon1, lat2, lon2);
    }
    
    @Override
    public Map<String, Object> getGeoPoint(String geoPointId) {
        logger.info("Retrieving geo point: {}", geoPointId);
        
        GeoPoint geoPoint = geoPointRepository.findById(geoPointId)
            .orElseThrow(() -> new GeoServiceException("Geo point not found: " + geoPointId));
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", geoPoint.getId());
        result.put("latitude", geoPoint.getLatitude());
        result.put("longitude", geoPoint.getLongitude());
        result.put("description", geoPoint.getDescription());
        
        return result;
    }
    
    @Override
    public boolean validateGeoData(Map<String, Object> geoData) {
        logger.info("Validating geo data");
        
        if (geoData == null) {
            return false;
        }
        
        Double latitude = (Double) geoData.get("latitude");
        Double longitude = (Double) geoData.get("longitude");
        
        if (latitude == null || longitude == null) {
            return false;
        }
        
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }
}
```

#### Schritt 2.4: REST-Controller implementieren

```java
// src/main/java/org/apache/ofbiz/geo/controller/GeoController.java
package org.apache.ofbiz.geo.controller;

import org.apache.ofbiz.geo.dto.DistanceRequest;
import org.apache.ofbiz.geo.dto.DistanceResponse;
import org.apache.ofbiz.geo.service.GeoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/geo")
public class GeoController {
    
    private static final Logger logger = LoggerFactory.getLogger(GeoController.class);
    
    private final GeoService geoService;
    
    public GeoController(GeoService geoService) {
        this.geoService = geoService;
    }
    
    @PostMapping("/distance")
    public ResponseEntity<DistanceResponse> calculateDistance(@RequestBody DistanceRequest request) {
        logger.info("Calculate distance request: {}", request);
        
        double distance = geoService.calculateDistance(
            request.getLat1(),
            request.getLon1(),
            request.getLat2(),
            request.getLon2()
        );
        
        DistanceResponse response = new DistanceResponse();
        response.setDistance(distance);
        response.setUnit("km");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/point/{id}")
    public ResponseEntity<?> getGeoPoint(@PathVariable String id) {
        logger.info("Get geo point: {}", id);
        
        try {
            return ResponseEntity.ok(geoService.getGeoPoint(id));
        } catch (Exception e) {
            logger.error("Error retrieving geo point", e);
            return ResponseEntity.notFound().build();
        }
    }
}
```

#### Schritt 2.5: Unit-Tests schreiben

```java
// src/test/java/org/apache/ofbiz/geo/service/GeoServiceImplTest.java
package org.apache.ofbiz.geo.service;

import org.apache.ofbiz.geo.entity.GeoPoint;
import org.apache.ofbiz.geo.repository.GeoPointRepository;
import org.apache.ofbiz.geo.util.GeoWorker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GeoService Tests")
class GeoServiceImplTest {
    
    private GeoService geoService;
    
    @Mock
    private GeoPointRepository geoPointRepository;
    
    @Mock
    private GeoWorker geoWorker;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        geoService = new GeoServiceImpl(geoPointRepository, geoWorker);
    }
    
    @Test
    @DisplayName("Should calculate distance between two points")
    void testCalculateDistance() {
        // Arrange
        when(geoWorker.calculateDistance(0, 0, 1, 1)).thenReturn(157.25);
        
        // Act
        double distance = geoService.calculateDistance(0, 0, 1, 1);
        
        // Assert
        assertEquals(157.25, distance);
        verify(geoWorker, times(1)).calculateDistance(0, 0, 1, 1);
    }
    
    @Test
    @DisplayName("Should validate correct geo data")
    void testValidateGeoDataSuccess() {
        // Arrange
        Map<String, Object> geoData = new HashMap<>();
        geoData.put("latitude", 52.5);
        geoData.put("longitude", 13.4);
        
        // Act
        boolean result = geoService.validateGeoData(geoData);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should reject invalid latitude")
    void testValidateGeoDataInvalidLatitude() {
        // Arrange
        Map<String, Object> geoData = new HashMap<>();
        geoData.put("latitude", 95.0); // > 90
        geoData.put("longitude", 13.4);
        
        // Act
        boolean result = geoService.validateGeoData(geoData);
        
        // Assert
        assertFalse(result);
    }
}
```

#### Schritt 2.6: Docker-Image erstellen

```dockerfile
# Dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/geo-service-1.0.0.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'

services:
  geo-db:
    image: postgres:14
    environment:
      POSTGRES_DB: geo_service
      POSTGRES_USER: geo_user
      POSTGRES_PASSWORD: geo_password
    ports:
      - "5432:5432"
    volumes:
      - geo_db_data:/var/lib/postgresql/data

  geo-service:
    build: .
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://geo-db:5432/geo_service
      SPRING_DATASOURCE_USERNAME: geo_user
      SPRING_DATASOURCE_PASSWORD: geo_password
      SPRING_JPA_HIBERNATE_DDL_AUTO: validate
    depends_on:
      - geo-db

volumes:
  geo_db_data:
```

---

### Phase 3: Implementierung (Woche 3-4)

#### Schritt 3.1: Datenbank-Migration durchführen

```sql
-- src/main/resources/db/migration/V1__initial_schema.sql

CREATE TABLE geo_point (
    id VARCHAR(255) PRIMARY KEY,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE geo_region (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_geo_point_coords ON geo_point(latitude, longitude);
CREATE INDEX idx_geo_region_name ON geo_region(name);
```

#### Schritt 3.2: Integration-Tests durchführen

```java
// src/test/java/org/apache/ofbiz/geo/GeoServiceApplicationTests.java
package org.apache.ofbiz.geo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class GeoServiceApplicationTests {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
        .withDatabaseName("geo_service_test")
        .withUsername("test")
        .withPassword("test");
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCalculateDistanceEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/geo/distance")
            .contentType("application/json")
            .content("{\"lat1\": 0, \"lon1\": 0, \"lat2\": 1, \"lon2\": 1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.distance").exists())
            .andExpect(jsonPath("$.unit").value("km"));
    }
}
```

#### Schritt 3.3: Performance-Tests durchführen

```java
// Performance-Test für Distanzberechnung
@Test
@DisplayName("Performance: Calculate 10000 distances")
void testPerformanceCalculateDistance() {
    long startTime = System.currentTimeMillis();
    
    for (int i = 0; i < 10000; i++) {
        geoService.calculateDistance(0, 0, 1, 1);
    }
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    // Sollte weniger als 1 Sekunde dauern
    assertTrue(duration < 1000, "Performance test failed: " + duration + "ms");
}
```

---

### Phase 4: Integration (Woche 5)

#### Schritt 4.1: OFBiz-Wrapper-Service erstellen

```java
// In OFBiz: GeoServiceWrapper.java
package org.apache.ofbiz.common.geo;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.service.DispatchContext;
import java.util.Map;
import java.util.HashMap;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.google.gson.Gson;

public class GeoServiceWrapper {
    
    private static final String GEO_SERVICE_URL = "http://geo-service:8081/api/v1/geo";
    private static final Gson gson = new Gson();
    
    public static Map<String, Object> calculateDistance(DispatchContext dctx, Map<String, Object> context) {
        try {
            double lat1 = (Double) context.get("lat1");
            double lon1 = (Double) context.get("lon1");
            double lat2 = (Double) context.get("lat2");
            double lon2 = (Double) context.get("lon2");
            
            // REST-Call zum Geo-Service
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(GEO_SERVICE_URL + "/distance");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("lat1", lat1);
            requestBody.put("lon1", lon1);
            requestBody.put("lat2", lat2);
            requestBody.put("lon2", lon2);
            
            httpPost.setEntity(new StringEntity(gson.toJson(requestBody)));
            httpPost.setHeader("Content-Type", "application/json");
            
            // Response verarbeiten...
            
            Map<String, Object> result = new HashMap<>();
            result.put("distance", distance);
            return result;
            
        } catch (Exception e) {
            Debug.logError(e, "Error calling Geo Service", "GeoServiceWrapper");
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            return result;
        }
    }
}
```

#### Schritt 4.2: End-to-End Tests durchführen

```java
// End-to-End Test: OFBiz → Geo-Service
@Test
void testEndToEndGeoServiceCall() {
    // 1. Starten Sie Geo-Service
    // 2. Rufen Sie OFBiz-Service auf
    // 3. Verifizieren Sie, dass Geo-Service aufgerufen wurde
    // 4. Verifizieren Sie das Ergebnis
}
```

#### Schritt 4.3: Staging-Deployment

```bash
# 1. Build
mvn clean package

# 2. Docker-Image erstellen
docker build -t geo-service:1.0.0 .

# 3. In Staging deployen
docker-compose -f docker-compose.staging.yml up -d

# 4. Smoke-Tests durchführen
curl http://staging-geo-service:8081/api/v1/geo/distance \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"lat1": 0, "lon1": 0, "lat2": 1, "lon2": 1}'
```

#### Schritt 4.4: Production-Deployment

```bash
# 1. Kubernetes-Manifeste erstellen
kubectl apply -f geo-service-deployment.yaml
kubectl apply -f geo-service-service.yaml

# 2. Canary-Deployment durchführen
# - 10% Traffic zu neuem Service
# - Monitoring aktivieren
# - Schrittweise auf 100% erhöhen

# 3. Monitoring & Alerting aktivieren
# - Prometheus Metriken
# - Grafana Dashboards
# - Alert-Regeln
```

---

## 📊 Erfolgs-Metriken

### Technische Metriken

| Metrik | Ziel | Aktuell |
|--------|------|---------|
| Unit-Test Coverage | >80% | - |
| Integration-Test Coverage | >70% | - |
| API Response Time | <100ms | - |
| Database Query Time | <50ms | - |
| Availability | >99.9% | - |
| Error Rate | <0.1% | - |

### Geschäftliche Metriken

| Metrik | Ziel | Aktuell |
|--------|------|---------|
| Deployment-Zeit | <15 min | - |
| Rollback-Zeit | <5 min | - |
| Mean Time to Recovery | <15 min | - |
| Keine Regression | 100% | - |

---

## 🚨 Risiken & Mitigation

| Risiko | Wahrscheinlichkeit | Impact | Mitigation |
|--------|-------------------|--------|-----------|
| Datenbank-Migration schlägt fehl | Mittel | Hoch | Backup, Dry-Run, Rollback-Plan |
| Performance-Degradation | Niedrig | Mittel | Performance-Tests, Monitoring |
| Netzwerk-Fehler | Niedrig | Mittel | Retry-Logik, Circuit Breaker |
| Deployment-Fehler | Niedrig | Hoch | Staging-Test, Canary-Deployment |

---

## 📋 Checkliste

### Vor Implementierung
- [ ] Alle Anforderungen dokumentiert
- [ ] REST-API-Spezifikation erstellt
- [ ] Datenbank-Schema definiert
- [ ] Team trainiert

### Während Implementierung
- [ ] Code geschrieben
- [ ] Tests geschrieben
- [ ] Code-Review durchgeführt
- [ ] Performance-Tests bestanden

### Nach Implementierung
- [ ] Staging-Deployment erfolgreich
- [ ] Smoke-Tests bestanden
- [ ] Monitoring aktiv
- [ ] Dokumentation aktualisiert
- [ ] Team trainiert
- [ ] Production-Deployment erfolgreich

---

## 📞 Kontakt & Support

**Projekt-Lead:** [Name]  
**Tech-Lead:** [Name]  
**DevOps:** [Name]  

---

**Dokument:** GEO_SERVICE_IMPLEMENTATION_PLAYBOOK.md  
**Version:** 1.0  
**Datum:** 10. Januar 2026  
**Status:** ✅ Bereit für Implementierung
