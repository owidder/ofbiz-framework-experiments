# Implementierungs-Leitfaden: OFBiz zu Microservices

## Phase 1: Vorbereitung und Infrastruktur (Wochen 1-4)

### 1.1 Infrastruktur-Setup

#### Kubernetes Cluster
```yaml
# Empfohlene Konfiguration
- Nodes: 3-5 (je nach Last)
- CPU pro Node: 4-8 Cores
- RAM pro Node: 16-32 GB
- Storage: 100-500 GB (je nach Datenmenge)
- Managed Service: AWS EKS, Azure AKS oder Google GKE
```

#### Datenbank-Setup
```yaml
PostgreSQL:
  - Shared Database (Phase 1): 1 Instanz
  - Replicas: 2 (für HA)
  - Backup: Täglich, 30 Tage Retention
  - Monitoring: CloudWatch/Prometheus
  
Redis (Caching):
  - Cluster: 3 Nodes
  - Memory: 16 GB pro Node
  - Persistence: RDB + AOF
```

#### Message Queue
```yaml
Apache Kafka:
  - Broker: 3 Instanzen
  - Partitionen: 10-20 (je nach Service)
  - Replication Factor: 3
  - Retention: 7 Tage
  - Monitoring: Kafka Manager oder Confluent Control Center
```

### 1.2 API Gateway Setup

```yaml
Kong Configuration:
  - Instanzen: 2-3 (für HA)
  - Plugins:
    - Authentication (JWT, OAuth2)
    - Rate Limiting
    - Request/Response Transformation
    - Logging
    - Monitoring
  
Routing Rules:
  /api/parties/* → Party Service
  /api/products/* → Product Service
  /api/orders/* → Order Service
  /api/invoices/* → Accounting Service
  /api/manufacturing/* → Manufacturing Service
  /api/hr/* → HR Service
  /api/marketing/* → Marketing Service
  /api/workeffort/* → Work Effort Service
  /api/content/* → Content Service
```

### 1.3 Monitoring & Logging Stack

```yaml
Prometheus:
  - Scrape Interval: 15s
  - Retention: 15 Tage
  - Storage: 50-100 GB

Grafana:
  - Dashboards für jeden Service
  - Alerting Rules
  - Notification Channels (Slack, PagerDuty)

ELK Stack:
  - Elasticsearch: 3 Nodes, 100 GB Storage
  - Logstash: 2 Instanzen
  - Kibana: 1 Instanz
  
Jaeger (Distributed Tracing):
  - Collector: 2 Instanzen
  - Storage: Elasticsearch Backend
```

### 1.4 CI/CD Pipeline

```yaml
GitLab CI / GitHub Actions:
  - Build: Docker Image erstellen
  - Test: Unit Tests, Integration Tests
  - Security Scan: SAST, Dependency Check
  - Push: Docker Registry (ECR, Docker Hub)
  - Deploy: Kubernetes (Dev → Staging → Prod)
  - Rollback: Automatisch bei Fehler
```

---

## Phase 2: Basis-Services Extrahieren (Wochen 5-12)

### 2.1 Party Service Extrahieren

#### Schritt 1: Code-Struktur vorbereiten

```
party-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ofbiz/party/
│   │   │       ├── controller/
│   │   │       │   └── PartyController.java
│   │   │       ├── service/
│   │   │       │   ├── PartyService.java
│   │   │       │   ├── ContactService.java
│   │   │       │   └── AgreementService.java
│   │   │       ├── repository/
│   │   │       │   ├── PartyRepository.java
│   │   │       │   ├── ContactMechRepository.java
│   │   │       │   └── AgreementRepository.java
│   │   │       ├── entity/
│   │   │       │   ├── Party.java
│   │   │       │   ├── ContactMech.java
│   │   │       │   └── Agreement.java
│   │   │       ├── dto/
│   │   │       │   ├── PartyDTO.java
│   │   │       │   ├── ContactDTO.java
│   │   │       │   └── AgreementDTO.java
│   │   │       ├── event/
│   │   │       │   ├── PartyCreatedEvent.java
│   │   │       │   ├── PartyUpdatedEvent.java
│   │   │       │   └── PartyEventPublisher.java
│   │   │       └── config/
│   │   │           ├── KafkaConfig.java
│   │   │           └── SecurityConfig.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/
│   │           └── migration/
│   │               ├── V1__Initial_Schema.sql
│   │               └── V2__Add_Indexes.sql
│   └── test/
│       └── java/
│           └── com/ofbiz/party/
│               ├── controller/
│               │   └── PartyControllerTest.java
│               ├── service/
│               │   └── PartyServiceTest.java
│               └── repository/
│                   └── PartyRepositoryTest.java
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

#### Schritt 2: Spring Boot Projekt initialisieren

```xml
<!-- pom.xml -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.ofbiz</groupId>
    <artifactId>party-service</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.0</version>
    </parent>
    
    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        
        <!-- Kafka -->
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        
        <!-- Monitoring -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
        
        <!-- Logging -->
        <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
            <version>7.2</version>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <version>1.17.6</version>
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
```

#### Schritt 3: REST API definieren

```java
// PartyController.java
@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
public class PartyController {
    
    private final PartyService partyService;
    
    @GetMapping("/{id}")
    public ResponseEntity<PartyDTO> getParty(@PathVariable String id) {
        return ResponseEntity.ok(partyService.getParty(id));
    }
    
    @PostMapping
    public ResponseEntity<PartyDTO> createParty(@RequestBody CreatePartyRequest request) {
        PartyDTO party = partyService.createParty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(party);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PartyDTO> updateParty(
            @PathVariable String id,
            @RequestBody UpdatePartyRequest request) {
        return ResponseEntity.ok(partyService.updateParty(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParty(@PathVariable String id) {
        partyService.deleteParty(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/contacts")
    public ResponseEntity<List<ContactDTO>> getContacts(@PathVariable String id) {
        return ResponseEntity.ok(partyService.getContacts(id));
    }
}
```

#### Schritt 4: Event Publishing konfigurieren

```java
// PartyEventPublisher.java
@Component
@RequiredArgsConstructor
public class PartyEventPublisher {
    
    private final KafkaTemplate<String, PartyEvent> kafkaTemplate;
    private static final String TOPIC = "party-events";
    
    public void publishPartyCreated(Party party) {
        PartyCreatedEvent event = new PartyCreatedEvent(
            party.getId(),
            party.getName(),
            party.getType(),
            Instant.now()
        );
        kafkaTemplate.send(TOPIC, party.getId(), event);
    }
    
    public void publishPartyUpdated(Party party) {
        PartyUpdatedEvent event = new PartyUpdatedEvent(
            party.getId(),
            party.getName(),
            party.getType(),
            Instant.now()
        );
        kafkaTemplate.send(TOPIC, party.getId(), event);
    }
}
```

#### Schritt 5: Docker Image erstellen

```dockerfile
# Dockerfile
FROM openjdk:17-slim

WORKDIR /app

COPY target/party-service-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Schritt 6: Kubernetes Deployment

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: party-service
  namespace: ofbiz
spec:
  replicas: 2
  selector:
    matchLabels:
      app: party-service
  template:
    metadata:
      labels:
        app: party-service
    spec:
      containers:
      - name: party-service
        image: ofbiz/party-service:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: url
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: kafka-broker-0:9092,kafka-broker-1:9092,kafka-broker-2:9092
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"

---
apiVersion: v1
kind: Service
metadata:
  name: party-service
  namespace: ofbiz
spec:
  selector:
    app: party-service
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP
```

### 2.2 Product Service Extrahieren

Ähnliche Struktur wie Party Service, mit zusätzlichen Komponenten:

```
product-service/
├── src/main/java/com/ofbiz/product/
│   ├── controller/
│   │   ├── ProductController.java
│   │   ├── InventoryController.java
│   │   ├── FacilityController.java
│   │   └── PricingController.java
│   ├── service/
│   │   ├── ProductService.java
│   │   ├── InventoryService.java
│   │   ├── FacilityService.java
│   │   └── PricingService.java
│   ├── listener/
│   │   └── OrderEventListener.java  # Hört auf order.created Events
│   └── cache/
│       ├── ProductCacheManager.java
│       └── PricingCacheManager.java
```

### 2.3 Content Service Extrahieren

```
content-service/
├── src/main/java/com/ofbiz/content/
│   ├── controller/
│   │   ├── ContentController.java
│   │   ├── DocumentController.java
│   │   └── SurveyController.java
│   ├── service/
│   │   ├── ContentService.java
│   │   ├── DocumentService.java
│   │   └── SurveyService.java
│   └── storage/
│       ├── S3StorageService.java
│       └── LocalStorageService.java
```

---

## Phase 3: Transaktionale Services (Wochen 13-24)

### 3.1 Order Service Extrahieren

```
order-service/
├── src/main/java/com/ofbiz/order/
│   ├── controller/
│   │   ├── OrderController.java
│   │   ├── CartController.java
│   │   ├── QuoteController.java
│   │   └── ReturnController.java
│   ├── service/
│   │   ├── OrderService.java
│   │   ├── CartService.java
│   │   ├── QuoteService.java
│   │   └── ReturnService.java
│   ├── saga/
│   │   ├── OrderSaga.java  # Saga Orchestrator
│   │   ├── OrderSagaStep.java
│   │   └── OrderSagaCompensation.java
│   ├── listener/
│   │   ├── PaymentEventListener.java
│   │   ├── InventoryEventListener.java
│   │   └── ManufacturingEventListener.java
│   └── client/
│       ├── ProductServiceClient.java
│       ├── AccountingServiceClient.java
│       └── ManufacturingServiceClient.java
```

#### Order Saga Implementation

```java
// OrderSaga.java
@Component
@RequiredArgsConstructor
public class OrderSaga {
    
    private final OrderService orderService;
    private final ProductServiceClient productClient;
    private final AccountingServiceClient accountingClient;
    private final ManufacturingServiceClient manufacturingClient;
    
    @Transactional
    public void executeOrderCreationSaga(CreateOrderRequest request) {
        Order order = null;
        
        try {
            // Step 1: Create Order
            order = orderService.createOrder(request);
            
            // Step 2: Reserve Inventory
            try {
                productClient.reserveInventory(order.getId(), request.getItems());
            } catch (Exception e) {
                compensateOrderCreation(order);
                throw new SagaException("Inventory reservation failed", e);
            }
            
            // Step 3: Create Invoice
            try {
                accountingClient.createInvoice(order.getId(), order.getTotalAmount());
            } catch (Exception e) {
                compensateInventoryReservation(order);
                compensateOrderCreation(order);
                throw new SagaException("Invoice creation failed", e);
            }
            
            // Step 4: Plan Manufacturing (if needed)
            if (order.requiresManufacturing()) {
                try {
                    manufacturingClient.planProduction(order.getId());
                } catch (Exception e) {
                    compensateInvoiceCreation(order);
                    compensateInventoryReservation(order);
                    compensateOrderCreation(order);
                    throw new SagaException("Manufacturing planning failed", e);
                }
            }
            
            // All steps successful
            orderService.confirmOrder(order.getId());
            
        } catch (SagaException e) {
            log.error("Order creation saga failed", e);
            throw e;
        }
    }
    
    private void compensateOrderCreation(Order order) {
        orderService.cancelOrder(order.getId());
    }
    
    private void compensateInventoryReservation(Order order) {
        productClient.releaseInventoryReservation(order.getId());
    }
    
    private void compensateInvoiceCreation(Order order) {
        accountingClient.cancelInvoice(order.getId());
    }
}
```

### 3.2 Accounting Service Extrahieren

```
accounting-service/
├── src/main/java/com/ofbiz/accounting/
│   ├── controller/
│   │   ├── InvoiceController.java
│   │   ├── PaymentController.java
│   │   ├── LedgerController.java
│   │   └── TaxController.java
│   ├── service/
│   │   ├── InvoiceService.java
│   │   ├── PaymentService.java
│   │   ├── LedgerService.java
│   │   └── TaxService.java
│   ├── gateway/
│   │   ├── PaymentGateway.java
│   │   ├── PayPalGateway.java
│   │   ├── StripeGateway.java
│   │   └── AuthorizeNetGateway.java
│   ├── listener/
│   │   └── OrderEventListener.java
│   └── client/
│       └── OrderServiceClient.java
```

### 3.3 Manufacturing Service Extrahieren

```
manufacturing-service/
├── src/main/java/com/ofbiz/manufacturing/
│   ├── controller/
│   │   ├── ProductionRunController.java
│   │   ├── BomController.java
│   │   ├── RoutingController.java
│   │   └── MrpController.java
│   ├── service/
│   │   ├── ProductionRunService.java
│   │   ├── BomService.java
│   │   ├── RoutingService.java
│   │   └── MrpService.java
│   ├── listener/
│   │   ├── OrderEventListener.java
│   │   └── InventoryEventListener.java
│   └── client/
│       ├── ProductServiceClient.java
│       └── OrderServiceClient.java
```

---

## Phase 4: Support-Services (Wochen 25-32)

### 4.1 HR Service

```
hr-service/
├── src/main/java/com/ofbiz/hr/
│   ├── controller/
│   │   ├── EmployeeController.java
│   │   ├── EmploymentController.java
│   │   ├── PositionController.java
│   │   └── SkillController.java
│   └── service/
│       ├── EmployeeService.java
│       ├── EmploymentService.java
│       ├── PositionService.java
│       └── SkillService.java
```

### 4.2 Marketing Service

```
marketing-service/
├── src/main/java/com/ofbiz/marketing/
│   ├── controller/
│   │   ├── CampaignController.java
│   │   ├── OpportunityController.java
│   │   └── LeadController.java
│   ├── service/
│   │   ├── CampaignService.java
│   │   ├── OpportunityService.java
│   │   └── LeadService.java
│   ├── listener/
│   │   ├── OrderEventListener.java
│   │   └── PartyEventListener.java
│   └── client/
│       ├── PartyServiceClient.java
│       └── ProductServiceClient.java
```

### 4.3 Work Effort Service

```
work-effort-service/
├── src/main/java/com/ofbiz/workeffort/
│   ├── controller/
│   │   ├── ProjectController.java
│   │   ├── TaskController.java
│   │   └── TimesheetController.java
│   ├── service/
│   │   ├── ProjectService.java
│   │   ├── TaskService.java
│   │   └── TimesheetService.java
│   └── client/
│       ├── PartyServiceClient.java
│       └── HrServiceClient.java
```

---

## Phase 5: Optimierung und Migration (Wochen 33+)

### 5.1 Database per Service Migration

#### Schritt 1: Datenbank-Replikation einrichten

```sql
-- Shared Database (Quelle)
CREATE PUBLICATION party_pub FOR TABLE party, contact_mech, agreement;

-- Party Service Database (Ziel)
CREATE SUBSCRIPTION party_sub CONNECTION 'dbname=shared_db' 
  PUBLICATION party_pub;
```

#### Schritt 2: Service-spezifische Datenbank initialisieren

```yaml
# party-service/src/main/resources/db/migration/V1__Initial_Schema.sql
CREATE TABLE party (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE contact_mech (
    id VARCHAR(20) PRIMARY KEY,
    party_id VARCHAR(20) NOT NULL REFERENCES party(id),
    type VARCHAR(50) NOT NULL,
    value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contact_mech_party_id ON contact_mech(party_id);
```

#### Schritt 3: Datenbank-Umschaltung

```java
// DatabaseMigrationService.java
@Component
@RequiredArgsConstructor
public class DatabaseMigrationService {
    
    private final JdbcTemplate jdbcTemplate;
    private final PartyRepository partyRepository;
    
    @Scheduled(cron = "0 2 * * *")  // Täglich um 02:00 Uhr
    public void migrateToServiceDatabase() {
        log.info("Starting database migration for Party Service");
        
        // Step 1: Daten aus Shared DB lesen
        List<Party> parties = readFromSharedDatabase();
        
        // Step 2: In Service-DB schreiben
        parties.forEach(partyRepository::save);
        
        // Step 3: Validierung
        long sharedDbCount = countInSharedDatabase();
        long serviceDbCount = partyRepository.count();
        
        if (sharedDbCount == serviceDbCount) {
            log.info("Database migration completed successfully");
            // Step 4: Umschalten auf Service-DB
            switchToServiceDatabase();
        } else {
            log.error("Database migration failed: count mismatch");
            throw new MigrationException("Count mismatch");
        }
    }
    
    private void switchToServiceDatabase() {
        // Update application.yml to use service-specific database
        // Restart service
    }
}
```

### 5.2 Performance-Tuning

#### Caching-Strategie

```java
// CacheConfiguration.java
@Configuration
@EnableCaching
public class CacheConfiguration {
    
    @Bean
    public CacheManager cacheManager() {
        return new RedisCacheManager(
            RedisCacheWriter.create(connectionFactory()),
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
        );
    }
    
    @Bean
    public LettuceConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory();
    }
}

// PartyService.java
@Service
@RequiredArgsConstructor
public class PartyService {
    
    private final PartyRepository partyRepository;
    
    @Cacheable(value = "parties", key = "#id")
    public PartyDTO getParty(String id) {
        return partyRepository.findById(id)
            .map(this::toDTO)
            .orElseThrow(() -> new PartyNotFoundException(id));
    }
    
    @CacheEvict(value = "parties", key = "#id")
    public PartyDTO updateParty(String id, UpdatePartyRequest request) {
        Party party = partyRepository.findById(id)
            .orElseThrow(() -> new PartyNotFoundException(id));
        
        party.setName(request.getName());
        party.setType(request.getType());
        
        return toDTO(partyRepository.save(party));
    }
}
```

#### Database Query Optimization

```java
// PartyRepository.java
@Repository
public interface PartyRepository extends JpaRepository<Party, String> {
    
    @Query("SELECT p FROM Party p LEFT JOIN FETCH p.contacts WHERE p.id = :id")
    Optional<Party> findByIdWithContacts(@Param("id") String id);
    
    @Query("SELECT p FROM Party p WHERE p.type = :type")
    @EntityGraph(attributePaths = {"contacts", "agreements"})
    List<Party> findByTypeWithRelations(@Param("type") String type);
}
```

### 5.3 Monitoring und Observability

#### Prometheus Metriken

```java
// MetricsConfiguration.java
@Configuration
public class MetricsConfiguration {
    
    @Bean
    public MeterRegistry meterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
}

// PartyService.java
@Service
@RequiredArgsConstructor
public class PartyService {
    
    private final MeterRegistry meterRegistry;
    private final PartyRepository partyRepository;
    
    public PartyDTO getParty(String id) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            PartyDTO party = partyRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new PartyNotFoundException(id));
            
            meterRegistry.counter("party.get.success").increment();
            return party;
            
        } catch (Exception e) {
            meterRegistry.counter("party.get.error").increment();
            throw e;
        } finally {
            sample.stop(Timer.builder("party.get.duration")
                .register(meterRegistry));
        }
    }
}
```

#### Distributed Tracing

```java
// TracingConfiguration.java
@Configuration
public class TracingConfiguration {
    
    @Bean
    public Tracer tracer() {
        return new JaegerTracer.Builder("party-service")
            .registerExtractor(Format.HTTP_HEADERS, new HttpCodec())
            .registerInjector(Format.HTTP_HEADERS, new HttpCodec())
            .build();
    }
}

// PartyService.java
@Service
@RequiredArgsConstructor
public class PartyService {
    
    private final Tracer tracer;
    
    public PartyDTO getParty(String id) {
        try (Scope scope = tracer.buildSpan("getParty")
                .withTag("party.id", id)
                .startActive(true)) {
            
            return partyRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new PartyNotFoundException(id));
        }
    }
}
```

---

## Testing-Strategie

### Unit Tests

```java
// PartyServiceTest.java
@ExtendWith(MockitoExtension.class)
class PartyServiceTest {
    
    @Mock
    private PartyRepository partyRepository;
    
    @InjectMocks
    private PartyService partyService;
    
    @Test
    void testGetParty_Success() {
        // Arrange
        String partyId = "PARTY-001";
        Party party = new Party(partyId, "Acme Corp", "ORGANIZATION");
        when(partyRepository.findById(partyId)).thenReturn(Optional.of(party));
        
        // Act
        PartyDTO result = partyService.getParty(partyId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(partyId);
        assertThat(result.getName()).isEqualTo("Acme Corp");
    }
}
```

### Integration Tests

```java
// PartyServiceIntegrationTest.java
@SpringBootTest
@Testcontainers
class PartyServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
        .withDatabaseName("test_db")
        .withUsername("test")
        .withPassword("test");
    
    @Autowired
    private PartyService partyService;
    
    @Test
    void testCreateAndRetrieveParty() {
        // Arrange
        CreatePartyRequest request = new CreatePartyRequest("Acme Corp", "ORGANIZATION");
        
        // Act
        PartyDTO created = partyService.createParty(request);
        PartyDTO retrieved = partyService.getParty(created.getId());
        
        // Assert
        assertThat(retrieved).isEqualTo(created);
    }
}
```

### Contract Tests (Pact)

```java
// PartyServiceContractTest.java
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "PartyService", port = "8080")
class PartyServiceContractTest {
    
    @Pact(consumer = "OrderService")
    public RequestResponsePact createPact(PactBuilder builder) {
        return builder
            .given("party with id PARTY-001 exists")
            .uponReceiving("a request for party details")
            .path("/api/parties/PARTY-001")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(new PactDslJsonBody()
                .stringValue("id", "PARTY-001")
                .stringValue("name", "Acme Corp")
                .stringValue("type", "ORGANIZATION"))
            .toPact();
    }
}
```

---

## Deployment-Checkliste

### Pre-Deployment

- [ ] Code Review durchgeführt
- [ ] Unit Tests: 100% bestanden
- [ ] Integration Tests: 100% bestanden
- [ ] Security Scan: Keine kritischen Fehler
- [ ] Performance Tests: Akzeptable Latenz
- [ ] Database Migration: Getestet
- [ ] Rollback Plan: Dokumentiert

### Deployment

- [ ] Backup der Produktionsdatenbank erstellt
- [ ] Blue-Green Environment vorbereitet
- [ ] Health Checks konfiguriert
- [ ] Monitoring Dashboards aktiviert
- [ ] Alerting Rules aktiviert
- [ ] Runbook für Incidents vorbereitet

### Post-Deployment

- [ ] Service Health: OK
- [ ] Error Rate: < 0.1%
- [ ] Response Time: < 500ms (p95)
- [ ] Database Connections: Normal
- [ ] Kafka Lag: < 1000 Messages
- [ ] Logs: Keine Fehler
- [ ] Smoke Tests: Bestanden

---

## Rollback-Strategie

```yaml
Wenn kritische Fehler auftreten:

1. Fehler erkannt (Error Rate > 5%)
   ├─→ Alert an On-Call Engineer
   └─→ Automatisches Rollback initiieren

2. Traffic zurück zu alter Version
   ├─→ 100% Traffic → Blue (alte Version)
   └─→ Green (neue Version) isolieren

3. Incident Investigation
   ├─→ Logs analysieren
   ├─→ Metrics analysieren
   ├─→ Root Cause identifizieren
   └─→ Fix implementieren

4. Erneuter Deployment
   ├─→ Fix testen
   ├─→ Canary Deployment (5% Traffic)
   ├─→ Monitoring (1 Stunde)
   └─→ Vollständiger Rollout
```
