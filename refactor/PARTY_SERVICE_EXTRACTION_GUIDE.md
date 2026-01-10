# Party Service Extraction Guide

## Übersicht

Dieser Guide beschreibt detailliert die Extraktion des **Party Service** als ersten Microservice aus der OFBiz-Monolith-Anwendung. Der Party Service wurde als erster Kandidat gewählt, weil er:

- ✅ Relativ klein ist (44 Typen)
- ✅ Wenige Abhängigkeiten zu anderen Business-Modulen hat
- ✅ Ein fundamentaler Service ist, den viele andere Services benötigen
- ✅ Klare fachliche Abgrenzung hat (Kunden, Lieferanten, Kontakte)

## 1. Scope Definition

### 1.1 Fachliche Verantwortung

Der Party Service ist verantwortlich für:

- **Party Management**: Verwaltung von Parties (abstrakte Entität für Personen und Organisationen)
- **Person Management**: Natürliche Personen mit Namen, Geburtsdatum, etc.
- **PartyGroup Management**: Organisationen, Firmen
- **Contact Mechanisms**: Adressen, Telefonnummern, E-Mails
- **Party Relationships**: Beziehungen zwischen Parties
- **Party Roles**: Rollen wie Kunde, Lieferant, Mitarbeiter
- **Party Classifications**: Kategorisierung von Parties

### 1.2 Entities im Scope

Basierend auf der Neo4j-Analyse, folgende Hauptentitäten:

```
Party (Basis-Entity)
├── Person
├── PartyGroup
├── PartyRole
├── PartyRelationship
├── PartyClassification
└── ContactMech
    ├── PostalAddress
    ├── TelecomNumber
    └── EmailAddress
```

### 1.3 Out of Scope

Folgende Bereiche bleiben zunächst im Monolithen:

- Bestellhistorie (gehört zu Order Service)
- Rechnungen (gehört zu Accounting Service)
- Kommunikationshistorie (kann später extrahiert werden)

## 2. Technische Architektur

### 2.1 Technology Stack

```yaml
Framework: Spring Boot 3.2.x
Language: Java 17+
Build Tool: Gradle 8.x
Database: PostgreSQL 15+
Cache: Redis 7.x
API: REST (Spring MVC) + GraphQL (optional)
Documentation: OpenAPI 3.0 (Springdoc)
Testing: JUnit 5, Testcontainers
Messaging: Kafka (für Events)
```

### 2.2 Projektstruktur

```
party-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/apache/ofbiz/party/
│   │   │       ├── PartyServiceApplication.java
│   │   │       ├── domain/
│   │   │       │   ├── model/
│   │   │       │   │   ├── Party.java
│   │   │       │   │   ├── Person.java
│   │   │       │   │   ├── PartyGroup.java
│   │   │       │   │   ├── ContactMech.java
│   │   │       │   │   └── ...
│   │   │       │   ├── repository/
│   │   │       │   │   ├── PartyRepository.java
│   │   │       │   │   └── ...
│   │   │       │   └── service/
│   │   │       │       ├── PartyService.java
│   │   │       │       └── ...
│   │   │       ├── application/
│   │   │       │   ├── dto/
│   │   │       │   │   ├── PartyDTO.java
│   │   │       │   │   └── ...
│   │   │       │   ├── mapper/
│   │   │       │   │   └── PartyMapper.java
│   │   │       │   └── usecase/
│   │   │       │       ├── CreatePartyUseCase.java
│   │   │       │       └── ...
│   │   │       ├── infrastructure/
│   │   │       │   ├── rest/
│   │   │       │   │   ├── PartyController.java
│   │   │       │   │   └── ...
│   │   │       │   ├── messaging/
│   │   │       │   │   ├── PartyEventPublisher.java
│   │   │       │   │   └── ...
│   │   │       │   └── config/
│   │   │       │       ├── DatabaseConfig.java
│   │   │       │       ├── SecurityConfig.java
│   │   │       │       └── ...
│   │   │       └── adapter/
│   │   │           └── ofbiz/
│   │   │               └── OFBizPartyAdapter.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── db/migration/
│   │       │   ├── V1__create_party_tables.sql
│   │       │   └── ...
│   │       └── openapi/
│   │           └── party-api.yaml
│   └── test/
│       ├── java/
│       │   └── org/apache/ofbiz/party/
│       │       ├── integration/
│       │       ├── unit/
│       │       └── e2e/
│       └── resources/
├── build.gradle
├── Dockerfile
├── docker-compose.yml
└── README.md
```

### 2.3 Domain Model

```java
// Party.java (Aggregate Root)
@Entity
@Table(name = "party")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Party {
    @Id
    private String partyId;
    
    @Enumerated(EnumType.STRING)
    private PartyType partyType;
    
    private String externalId;
    private String description;
    private String statusId;
    
    @CreatedDate
    private Instant createdDate;
    
    @LastModifiedDate
    private Instant lastModifiedDate;
    
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL)
    private Set<ContactMech> contactMechs = new HashSet<>();
    
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL)
    private Set<PartyRole> roles = new HashSet<>();
    
    // Business methods
    public void addContactMech(ContactMech contactMech) {
        contactMechs.add(contactMech);
        contactMech.setParty(this);
    }
    
    public void addRole(PartyRole role) {
        roles.add(role);
        role.setParty(this);
    }
}

// Person.java
@Entity
@Table(name = "person")
@PrimaryKeyJoinColumn(name = "party_id")
public class Person extends Party {
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private LocalDate birthDate;
    
    // Getters, setters, business methods
}

// PartyGroup.java
@Entity
@Table(name = "party_group")
@PrimaryKeyJoinColumn(name = "party_id")
public class PartyGroup extends Party {
    private String groupName;
    private String groupNameLocal;
    private String officeSiteName;
    private Integer numEmployees;
    private BigDecimal annualRevenue;
    
    // Getters, setters, business methods
}

// ContactMech.java
@Entity
@Table(name = "contact_mech")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ContactMech {
    @Id
    private String contactMechId;
    
    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;
    
    @Enumerated(EnumType.STRING)
    private ContactMechType contactMechType;
    
    private LocalDate fromDate;
    private LocalDate thruDate;
    
    // Getters, setters
}

// PostalAddress.java
@Entity
@Table(name = "postal_address")
@PrimaryKeyJoinColumn(name = "contact_mech_id")
public class PostalAddress extends ContactMech {
    private String toName;
    private String attnName;
    private String address1;
    private String address2;
    private String city;
    private String stateProvinceGeoId;
    private String postalCode;
    private String countryGeoId;
    
    // Getters, setters, validation
}
```

## 3. API Design

### 3.1 REST Endpoints

```yaml
# Party Management
GET    /api/v1/parties                    # List parties (paginated)
GET    /api/v1/parties/{partyId}          # Get party by ID
POST   /api/v1/parties                    # Create party
PUT    /api/v1/parties/{partyId}          # Update party
DELETE /api/v1/parties/{partyId}          # Delete party (soft delete)

# Person specific
POST   /api/v1/parties/persons            # Create person
PUT    /api/v1/parties/persons/{partyId}  # Update person

# PartyGroup specific
POST   /api/v1/parties/groups             # Create party group
PUT    /api/v1/parties/groups/{partyId}   # Update party group

# Contact Mechanisms
GET    /api/v1/parties/{partyId}/contacts              # List contacts
POST   /api/v1/parties/{partyId}/contacts              # Add contact
PUT    /api/v1/parties/{partyId}/contacts/{contactId}  # Update contact
DELETE /api/v1/parties/{partyId}/contacts/{contactId}  # Remove contact

# Party Roles
GET    /api/v1/parties/{partyId}/roles                 # List roles
POST   /api/v1/parties/{partyId}/roles                 # Add role
DELETE /api/v1/parties/{partyId}/roles/{roleTypeId}    # Remove role

# Search
GET    /api/v1/parties/search?q={query}                # Search parties
GET    /api/v1/parties/search/advanced                 # Advanced search
```

### 3.2 Example Request/Response

```json
// POST /api/v1/parties/persons
{
  "firstName": "John",
  "lastName": "Doe",
  "gender": "M",
  "birthDate": "1980-05-15",
  "contactMechs": [
    {
      "contactMechType": "EMAIL_ADDRESS",
      "infoString": "john.doe@example.com"
    },
    {
      "contactMechType": "POSTAL_ADDRESS",
      "address1": "123 Main St",
      "city": "Springfield",
      "stateProvinceGeoId": "USA_IL",
      "postalCode": "62701",
      "countryGeoId": "USA"
    }
  ],
  "roles": [
    {
      "roleTypeId": "CUSTOMER"
    }
  ]
}

// Response: 201 Created
{
  "partyId": "10000",
  "partyType": "PERSON",
  "firstName": "John",
  "lastName": "Doe",
  "gender": "M",
  "birthDate": "1980-05-15",
  "statusId": "PARTY_ENABLED",
  "contactMechs": [
    {
      "contactMechId": "10001",
      "contactMechType": "EMAIL_ADDRESS",
      "infoString": "john.doe@example.com",
      "fromDate": "2026-01-10"
    },
    {
      "contactMechId": "10002",
      "contactMechType": "POSTAL_ADDRESS",
      "address1": "123 Main St",
      "city": "Springfield",
      "stateProvinceGeoId": "USA_IL",
      "postalCode": "62701",
      "countryGeoId": "USA",
      "fromDate": "2026-01-10"
    }
  ],
  "roles": [
    {
      "roleTypeId": "CUSTOMER",
      "fromDate": "2026-01-10"
    }
  ],
  "createdDate": "2026-01-10T15:30:00Z",
  "lastModifiedDate": "2026-01-10T15:30:00Z",
  "_links": {
    "self": { "href": "/api/v1/parties/10000" },
    "contacts": { "href": "/api/v1/parties/10000/contacts" },
    "roles": { "href": "/api/v1/parties/10000/roles" }
  }
}
```

## 4. Migration Strategy

### 4.1 Phase 1: Setup & Dual Write (Woche 1-2)

**Ziel:** Infrastruktur aufsetzen, Service implementieren, Dual-Write aktivieren

**Schritte:**

1. **Projekt Setup**
   ```bash
   # Projekt erstellen
   spring init --dependencies=web,data-jpa,postgresql,redis,kafka \
                --group-id=org.apache.ofbiz \
                --artifact-id=party-service \
                --name=PartyService \
                party-service
   ```

2. **Datenbank-Schema migrieren**
   ```sql
   -- V1__create_party_tables.sql
   CREATE TABLE party (
       party_id VARCHAR(20) PRIMARY KEY,
       party_type VARCHAR(20) NOT NULL,
       external_id VARCHAR(50),
       description TEXT,
       status_id VARCHAR(20),
       created_date TIMESTAMP NOT NULL,
       last_modified_date TIMESTAMP NOT NULL
   );
   
   CREATE TABLE person (
       party_id VARCHAR(20) PRIMARY KEY REFERENCES party(party_id),
       first_name VARCHAR(100),
       middle_name VARCHAR(100),
       last_name VARCHAR(100),
       gender VARCHAR(1),
       birth_date DATE
   );
   
   -- ... weitere Tabellen
   ```

3. **OFBiz Adapter implementieren**
   ```java
   @Component
   public class OFBizPartyAdapter {
       private final Delegator delegator;
       private final PartyService partyService;
       
       @Async
       public void syncPartyToNewService(GenericValue partyValue) {
           // Konvertiere OFBiz GenericValue zu DTO
           PartyDTO dto = convertToDTO(partyValue);
           
           // Sende an neuen Service
           partyService.createOrUpdateParty(dto);
       }
   }
   ```

4. **OFBiz Service-Hook einbauen**
   ```java
   // In OFBiz: PartyServices.java
   public static Map<String, Object> createParty(DispatchContext dctx, 
                                                   Map<String, ?> context) {
       // Bestehende Logik
       GenericValue party = delegator.create("Party", context);
       
       // NEU: Sync zu neuem Service
       partyAdapter.syncPartyToNewService(party);
       
       return ServiceUtil.returnSuccess();
   }
   ```

### 4.2 Phase 2: Dual Read & Validation (Woche 3-4)

**Ziel:** Lesezugriffe auf neuen Service umstellen, Datenvalidierung

**Schritte:**

1. **Shadow Read implementieren**
   ```java
   @Service
   public class PartyReadService {
       public PartyDTO getParty(String partyId) {
           // Lese aus neuem Service
           PartyDTO newParty = partyServiceClient.getParty(partyId);
           
           // Lese aus OFBiz (zur Validierung)
           GenericValue oldParty = delegator.findOne("Party", 
               UtilMisc.toMap("partyId", partyId), false);
           
           // Vergleiche und logge Unterschiede
           validateConsistency(newParty, oldParty);
           
           return newParty;
       }
   }
   ```

2. **Daten-Migration**
   ```java
   @Component
   public class PartyDataMigration {
       @Scheduled(cron = "0 0 2 * * ?") // Täglich um 2 Uhr
       public void migrateParties() {
           List<GenericValue> parties = delegator.findAll("Party", false);
           
           for (GenericValue party : parties) {
               try {
                   syncPartyToNewService(party);
               } catch (Exception e) {
                   log.error("Failed to migrate party: " + 
                             party.getString("partyId"), e);
               }
           }
       }
   }
   ```

3. **Monitoring & Alerting**
   ```yaml
   # Prometheus metrics
   party_service_sync_success_total
   party_service_sync_failure_total
   party_service_data_consistency_errors_total
   party_service_response_time_seconds
   ```

### 4.3 Phase 3: Cutover (Woche 5-6)

**Ziel:** Vollständige Umstellung auf neuen Service

**Schritte:**

1. **Feature Flag aktivieren**
   ```java
   @Configuration
   public class FeatureFlags {
       @Value("${feature.party-service.enabled:false}")
       private boolean partyServiceEnabled;
       
       public boolean useNewPartyService() {
           return partyServiceEnabled;
       }
   }
   ```

2. **Schrittweise Umstellung**
   - Tag 1: 10% Traffic auf neuen Service
   - Tag 2: 25% Traffic
   - Tag 3: 50% Traffic
   - Tag 4: 75% Traffic
   - Tag 5: 100% Traffic

3. **OFBiz Party-Tabellen read-only**
   ```sql
   -- Revoke write permissions
   REVOKE INSERT, UPDATE, DELETE ON party FROM ofbiz_app_user;
   GRANT SELECT ON party TO ofbiz_app_user;
   ```

## 5. Testing Strategy

### 5.1 Unit Tests

```java
@SpringBootTest
class PartyServiceTest {
    @Autowired
    private PartyService partyService;
    
    @Test
    void shouldCreatePerson() {
        // Given
        PersonDTO person = PersonDTO.builder()
            .firstName("John")
            .lastName("Doe")
            .build();
        
        // When
        PartyDTO created = partyService.createPerson(person);
        
        // Then
        assertThat(created.getPartyId()).isNotNull();
        assertThat(created.getFirstName()).isEqualTo("John");
    }
}
```

### 5.2 Integration Tests

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class PartyControllerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:15");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateAndRetrieveParty() {
        // Create
        PersonDTO person = new PersonDTO("John", "Doe");
        ResponseEntity<PartyDTO> createResponse = 
            restTemplate.postForEntity("/api/v1/parties/persons", 
                                       person, PartyDTO.class);
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String partyId = createResponse.getBody().getPartyId();
        
        // Retrieve
        ResponseEntity<PartyDTO> getResponse = 
            restTemplate.getForEntity("/api/v1/parties/" + partyId, 
                                      PartyDTO.class);
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getFirstName()).isEqualTo("John");
    }
}
```

### 5.3 E2E Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class PartyE2ETest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void completePartyLifecycle() throws Exception {
        // Create person
        String createRequest = """
            {
              "firstName": "Jane",
              "lastName": "Smith",
              "contactMechs": [
                {
                  "contactMechType": "EMAIL_ADDRESS",
                  "infoString": "jane@example.com"
                }
              ]
            }
            """;
        
        MvcResult createResult = mockMvc.perform(
            post("/api/v1/parties/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isCreated())
            .andReturn();
        
        String partyId = JsonPath.read(
            createResult.getResponse().getContentAsString(), 
            "$.partyId");
        
        // Add role
        mockMvc.perform(
            post("/api/v1/parties/" + partyId + "/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleTypeId\": \"CUSTOMER\"}"))
            .andExpect(status().isCreated());
        
        // Verify
        mockMvc.perform(get("/api/v1/parties/" + partyId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Jane"))
            .andExpect(jsonPath("$.roles[0].roleTypeId").value("CUSTOMER"));
    }
}
```

## 6. Monitoring & Observability

### 6.1 Metriken

```java
@Component
public class PartyMetrics {
    private final MeterRegistry registry;
    
    public PartyMetrics(MeterRegistry registry) {
        this.registry = registry;
    }
    
    public void recordPartyCreated(String partyType) {
        registry.counter("party.created", 
                        "type", partyType).increment();
    }
    
    public void recordSyncFailure(String reason) {
        registry.counter("party.sync.failure", 
                        "reason", reason).increment();
    }
}
```

### 6.2 Distributed Tracing

```java
@RestController
public class PartyController {
    private final Tracer tracer;
    
    @GetMapping("/parties/{partyId}")
    public PartyDTO getParty(@PathVariable String partyId) {
        Span span = tracer.nextSpan().name("getParty").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            span.tag("party.id", partyId);
            return partyService.getParty(partyId);
        } finally {
            span.end();
        }
    }
}
```

### 6.3 Health Checks

```java
@Component
public class PartyServiceHealthIndicator implements HealthIndicator {
    private final PartyRepository repository;
    
    @Override
    public Health health() {
        try {
            long count = repository.count();
            return Health.up()
                .withDetail("parties", count)
                .build();
        } catch (Exception e) {
            return Health.down()
                .withException(e)
                .build();
        }
    }
}
```

## 7. Rollback Plan

Falls Probleme auftreten:

1. **Feature Flag deaktivieren**
   ```yaml
   feature:
     party-service:
       enabled: false
   ```

2. **Traffic zurück zu OFBiz**
   - API Gateway Routing anpassen
   - Alle Requests wieder an OFBiz

3. **Daten-Sync rückwärts**
   - Falls Daten im neuen Service geändert wurden
   - Sync zurück zu OFBiz

4. **Post-Mortem**
   - Ursachenanalyse
   - Lessons Learned dokumentieren

## 8. Success Criteria

- ✅ Alle Party-CRUD-Operationen funktionieren über neuen Service
- ✅ Response Time < 200ms (p95)
- ✅ Availability > 99.9%
- ✅ Datenkonsistenz 100% (keine Sync-Fehler)
- ✅ Alle Tests grün (Unit, Integration, E2E)
- ✅ Monitoring & Alerting aktiv
- ✅ Dokumentation vollständig

## 9. Nächste Schritte nach Party Service

Nach erfolgreicher Extraktion des Party Service:

1. **Content Service** (ähnliche Komplexität)
2. **Product Service** (nutzt Party Service für Lieferanten)
3. **Order Service** (nutzt Party und Product Service)

## Ressourcen

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Strangler Fig Pattern](https://martinfowler.com/bliki/StranglerFigApplication.html)
- [Microservices Patterns](https://microservices.io/patterns/)
