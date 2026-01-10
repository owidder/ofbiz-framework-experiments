# OFBiz Dependency Visualizations

Diese Datei enthält Visualisierungen der Abhängigkeiten zwischen OFBiz-Modulen zur Unterstützung der Service-Dekomposition.

## 1. Business-Module Dependency Graph

```mermaid
graph TB
    %% Business Modules
    Party[Party Service<br/>44 Types]
    Content[Content Service<br/>336 Types]
    Product[Product Service<br/>360 Types]
    Order[Order Service<br/>200 Types]
    Accounting[Accounting Service<br/>168 Types]
    Shipment[Shipment Service<br/>104 Types]
    Manufacturing[Manufacturing Service<br/>52 Types]
    WorkEffort[WorkEffort Service<br/>104 Types]
    Marketing[Marketing Service<br/>12 Types]
    HumanRes[HumanRes Service<br/>4 Types]
    SFA[SFA Service<br/>4 Types]
    
    %% Dependencies with weights
    Order -->|188 deps| Product
    Accounting -->|56 deps| Order
    Product -->|48 deps| Content
    Accounting -->|40 deps| Product
    Order -->|36 deps| Party
    Shipment -->|16 deps| Party
    Shipment -->|16 deps| Product
    Manufacturing -->|16 deps| Product
    WorkEffort -->|12 deps| Content
    Order -->|12 deps| Content
    Party -->|12 deps| Content
    Accounting -->|8 deps| Party
    Product -->|8 deps| Party
    SFA -->|8 deps| Party
    HumanRes -->|4 deps| Party
    Marketing -->|4 deps| Product
    Manufacturing -->|4 deps| Order
    Order -->|4 deps| Marketing
    
    %% Styling
    classDef tier1 fill:#90EE90,stroke:#2E8B57,stroke-width:3px
    classDef tier2 fill:#FFD700,stroke:#DAA520,stroke-width:3px
    classDef tier3 fill:#FF6B6B,stroke:#C92A2A,stroke-width:3px
    
    class Party,Content,Marketing tier1
    class Product,WorkEffort,Shipment tier2
    class Order,Accounting,Manufacturing tier3
```

## 2. Framework Dependencies

```mermaid
graph TB
    %% Framework Layer
    Base[Base Framework<br/>1,572 Types]
    Entity[Entity Engine<br/>1,012 Types]
    Service[Service Engine<br/>620 Types]
    Widget[Widget Framework<br/>964 Types]
    WebApp[WebApp Framework<br/>372 Types]
    Security[Security<br/>56 Types]
    
    %% Business Layer
    Product[Product]
    Content[Content]
    Order[Order]
    Accounting[Accounting]
    Party[Party]
    Shipment[Shipment]
    WorkEffort[WorkEffort]
    
    %% Dependencies to Framework
    Product -->|1,196| Base
    Product -->|1,456| Entity
    Product -->|416| Service
    
    Content -->|1,460| Base
    Content -->|972| Entity
    Content -->|352| Service
    Content -->|96| Widget
    
    Order -->|884| Base
    Order -->|917| Entity
    Order -->|373| Service
    
    Accounting -->|724| Base
    Accounting -->|620| Entity
    Accounting -->|328| Service
    
    Party -->|220| Base
    Party -->|320| Entity
    Party -->|72| Service
    
    Shipment -->|436| Base
    Shipment -->|308| Entity
    Shipment -->|220| Service
    
    WorkEffort -->|292| Base
    WorkEffort -->|388| Entity
    WorkEffort -->|140| Service
    
    %% Styling
    classDef framework fill:#E0E0E0,stroke:#666,stroke-width:2px
    classDef business fill:#87CEEB,stroke:#4682B4,stroke-width:2px
    
    class Base,Entity,Service,Widget,WebApp,Security framework
    class Product,Content,Order,Accounting,Party,Shipment,WorkEffort business
```

## 3. Service Extraction Roadmap

```mermaid
gantt
    title OFBiz Service Extraction Timeline
    dateFormat YYYY-MM-DD
    section Phase 0: Prep
    Infrastructure Setup           :prep1, 2026-01-15, 4w
    API Gateway & Monitoring       :prep2, after prep1, 2w
    
    section Phase 1: Tier 1
    Party Service                  :tier1a, after prep2, 6w
    Content Service                :tier1b, after tier1a, 4w
    Marketing Service              :tier1c, after tier1b, 3w
    
    section Phase 2: Tier 2
    Product Service                :tier2a, after tier1c, 8w
    WorkEffort Service             :tier2b, after tier2a, 5w
    Shipment Service               :tier2c, after tier2b, 5w
    
    section Phase 3: Tier 3
    Order Service                  :tier3a, after tier2c, 12w
    Accounting Service             :tier3b, after tier3a, 10w
    Manufacturing Service          :tier3c, after tier3b, 6w
```

## 4. Bounded Context Map

```mermaid
graph TB
    subgraph "Customer Domain"
        PartyBC[Party<br/>Bounded Context]
        ContentBC1[Content<br/>Bounded Context]
        PartyBC -.->|ACL| ContentBC1
    end
    
    subgraph "Catalog Domain"
        ProductBC[Product<br/>Bounded Context]
        ContentBC2[Content<br/>Bounded Context]
        ProductBC -.->|ACL| ContentBC2
    end
    
    subgraph "Order Domain"
        OrderBC[Order<br/>Bounded Context]
        OrderBC -.->|API| ProductBC
        OrderBC -.->|API| PartyBC
        OrderBC -.->|API| MarketingBC[Marketing BC]
    end
    
    subgraph "Fulfillment Domain"
        ShipmentBC[Shipment<br/>Bounded Context]
        ShipmentBC -.->|API| PartyBC
        ShipmentBC -.->|API| ProductBC
    end
    
    subgraph "Finance Domain"
        AccountingBC[Accounting<br/>Bounded Context]
        AccountingBC -.->|API| OrderBC
        AccountingBC -.->|API| ProductBC
        AccountingBC -.->|API| PartyBC
    end
    
    subgraph "Manufacturing Domain"
        ManufacturingBC[Manufacturing<br/>Bounded Context]
        ManufacturingBC -.->|API| ProductBC
        ManufacturingBC -.->|API| OrderBC
    end
    
    %% Styling
    classDef bcStyle fill:#E8F4F8,stroke:#0066CC,stroke-width:2px
    class PartyBC,ProductBC,OrderBC,ShipmentBC,AccountingBC,ManufacturingBC,ContentBC1,ContentBC2,MarketingBC bcStyle
```

## 5. Data Flow Architecture

```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant Order as Order Service
    participant Product as Product Service
    participant Party as Party Service
    participant Kafka as Event Bus
    participant Accounting as Accounting Service
    
    Client->>Gateway: Create Order
    Gateway->>Order: POST /orders
    
    Order->>Product: GET /products/{id}
    Product-->>Order: Product Details
    
    Order->>Party: GET /parties/{id}
    Party-->>Order: Customer Details
    
    Order->>Order: Validate & Create Order
    Order->>Kafka: Publish OrderCreated Event
    Order-->>Gateway: Order Response
    Gateway-->>Client: 201 Created
    
    Kafka->>Accounting: OrderCreated Event
    Accounting->>Accounting: Create Invoice
    Accounting->>Kafka: Publish InvoiceCreated Event
```

## 6. Service Complexity Matrix

```mermaid
quadrantChart
    title Service Extraction Complexity vs Business Value
    x-axis Low Complexity --> High Complexity
    y-axis Low Value --> High Value
    quadrant-1 Do First
    quadrant-2 Strategic
    quadrant-3 Quick Wins
    quadrant-4 Reconsider
    
    Party: [0.2, 0.9]
    Content: [0.3, 0.7]
    Marketing: [0.15, 0.5]
    Product: [0.6, 0.95]
    WorkEffort: [0.4, 0.6]
    Shipment: [0.5, 0.7]
    Order: [0.85, 0.9]
    Accounting: [0.75, 0.85]
    Manufacturing: [0.65, 0.5]
    HumanRes: [0.2, 0.3]
    SFA: [0.25, 0.4]
```

## 7. Anti-Corruption Layer Pattern

```mermaid
graph LR
    subgraph "Legacy OFBiz"
        OFBizEntity[Entity Engine]
        OFBizService[Service Engine]
    end
    
    subgraph "Anti-Corruption Layer"
        Adapter1[Party Adapter]
        Adapter2[Product Adapter]
        Adapter3[Order Adapter]
    end
    
    subgraph "New Microservices"
        PartyMS[Party Service]
        ProductMS[Product Service]
        OrderMS[Order Service]
    end
    
    OFBizEntity --> Adapter1
    OFBizService --> Adapter1
    Adapter1 --> PartyMS
    
    OFBizEntity --> Adapter2
    OFBizService --> Adapter2
    Adapter2 --> ProductMS
    
    OFBizEntity --> Adapter3
    OFBizService --> Adapter3
    Adapter3 --> OrderMS
    
    %% Styling
    classDef legacy fill:#FFE4E1,stroke:#8B0000,stroke-width:2px
    classDef acl fill:#FFF8DC,stroke:#DAA520,stroke-width:2px
    classDef modern fill:#E0FFE0,stroke:#006400,stroke-width:2px
    
    class OFBizEntity,OFBizService legacy
    class Adapter1,Adapter2,Adapter3 acl
    class PartyMS,ProductMS,OrderMS modern
```

## 8. Event-Driven Architecture

```mermaid
graph TB
    subgraph "Event Producers"
        OrderSvc[Order Service]
        ProductSvc[Product Service]
        PartySvc[Party Service]
    end
    
    subgraph "Event Bus (Kafka)"
        Topic1[order-events]
        Topic2[product-events]
        Topic3[party-events]
    end
    
    subgraph "Event Consumers"
        AccountingSvc[Accounting Service]
        ShipmentSvc[Shipment Service]
        NotificationSvc[Notification Service]
        AnalyticsSvc[Analytics Service]
    end
    
    OrderSvc -->|OrderCreated<br/>OrderPaid<br/>OrderCancelled| Topic1
    ProductSvc -->|PriceChanged<br/>StockUpdated| Topic2
    PartySvc -->|CustomerCreated<br/>AddressChanged| Topic3
    
    Topic1 --> AccountingSvc
    Topic1 --> ShipmentSvc
    Topic1 --> NotificationSvc
    Topic1 --> AnalyticsSvc
    
    Topic2 --> NotificationSvc
    Topic2 --> AnalyticsSvc
    
    Topic3 --> NotificationSvc
    
    %% Styling
    classDef producer fill:#90EE90,stroke:#2E8B57,stroke-width:2px
    classDef bus fill:#FFD700,stroke:#DAA520,stroke-width:2px
    classDef consumer fill:#87CEEB,stroke:#4682B4,stroke-width:2px
    
    class OrderSvc,ProductSvc,PartySvc producer
    class Topic1,Topic2,Topic3 bus
    class AccountingSvc,ShipmentSvc,NotificationSvc,AnalyticsSvc consumer
```

## 9. Saga Pattern für Order Processing

```mermaid
stateDiagram-v2
    [*] --> OrderCreated: Create Order
    
    OrderCreated --> ReserveInventory: Reserve Stock
    ReserveInventory --> ProcessPayment: Stock Reserved
    ReserveInventory --> CompensateOrder: Stock Unavailable
    
    ProcessPayment --> CreateShipment: Payment Success
    ProcessPayment --> ReleaseInventory: Payment Failed
    
    CreateShipment --> OrderCompleted: Shipment Created
    CreateShipment --> RefundPayment: Shipment Failed
    
    OrderCompleted --> [*]
    
    ReleaseInventory --> CompensateOrder
    RefundPayment --> ReleaseInventory
    CompensateOrder --> OrderFailed
    OrderFailed --> [*]
    
    note right of OrderCreated
        Order Service
        publishes OrderCreated event
    end note
    
    note right of ReserveInventory
        Product Service
        reserves inventory
    end note
    
    note right of ProcessPayment
        Accounting Service
        processes payment
    end note
    
    note right of CreateShipment
        Shipment Service
        creates shipment
    end note
```

## 10. Deployment Architecture

```mermaid
graph TB
    subgraph "Load Balancer"
        LB[Nginx/AWS ALB]
    end
    
    subgraph "API Gateway Layer"
        Gateway[Kong/Spring Cloud Gateway]
    end
    
    subgraph "Service Mesh (Istio)"
        subgraph "Tier 1 Services"
            Party1[Party Service<br/>Instance 1]
            Party2[Party Service<br/>Instance 2]
            Content1[Content Service<br/>Instance 1]
        end
        
        subgraph "Tier 2 Services"
            Product1[Product Service<br/>Instance 1]
            Product2[Product Service<br/>Instance 2]
            Product3[Product Service<br/>Instance 3]
        end
        
        subgraph "Tier 3 Services"
            Order1[Order Service<br/>Instance 1]
            Order2[Order Service<br/>Instance 2]
            Accounting1[Accounting Service<br/>Instance 1]
        end
    end
    
    subgraph "Data Layer"
        PartyDB[(Party DB)]
        ProductDB[(Product DB)]
        OrderDB[(Order DB)]
        ContentStore[S3/MinIO]
        Cache[(Redis Cache)]
    end
    
    subgraph "Event Bus"
        Kafka[Apache Kafka]
    end
    
    subgraph "Observability"
        Prometheus[Prometheus]
        Grafana[Grafana]
        Jaeger[Jaeger Tracing]
        ELK[ELK Stack]
    end
    
    LB --> Gateway
    Gateway --> Party1
    Gateway --> Party2
    Gateway --> Content1
    Gateway --> Product1
    Gateway --> Product2
    Gateway --> Product3
    Gateway --> Order1
    Gateway --> Order2
    Gateway --> Accounting1
    
    Party1 --> PartyDB
    Party2 --> PartyDB
    Product1 --> ProductDB
    Product2 --> ProductDB
    Product3 --> ProductDB
    Order1 --> OrderDB
    Order2 --> OrderDB
    Content1 --> ContentStore
    
    Product1 --> Cache
    Product2 --> Cache
    Product3 --> Cache
    
    Order1 --> Kafka
    Order2 --> Kafka
    Product1 --> Kafka
    Accounting1 --> Kafka
    
    Party1 -.-> Prometheus
    Product1 -.-> Prometheus
    Order1 -.-> Prometheus
    Prometheus --> Grafana
    
    Party1 -.-> Jaeger
    Product1 -.-> Jaeger
    Order1 -.-> Jaeger
    
    Party1 -.-> ELK
    Product1 -.-> ELK
    Order1 -.-> ELK
```

## 11. Migration Phases Visualization

```mermaid
graph LR
    subgraph "Phase 1: Dual Write"
        Client1[Client]
        OFBiz1[OFBiz Legacy]
        NewSvc1[New Service]
        DB1[(Legacy DB)]
        DB2[(New DB)]
        
        Client1 --> OFBiz1
        OFBiz1 --> DB1
        OFBiz1 -.->|Sync| NewSvc1
        NewSvc1 --> DB2
    end
    
    subgraph "Phase 2: Dual Read"
        Client2[Client]
        OFBiz2[OFBiz Legacy]
        NewSvc2[New Service]
        DB3[(Legacy DB)]
        DB4[(New DB)]
        
        Client2 --> OFBiz2
        OFBiz2 -.->|Read| DB3
        OFBiz2 -->|Primary Read| NewSvc2
        NewSvc2 --> DB4
        OFBiz2 -->|Write| DB3
        OFBiz2 -.->|Sync| NewSvc2
    end
    
    subgraph "Phase 3: Full Migration"
        Client3[Client]
        Gateway3[API Gateway]
        NewSvc3[New Service]
        OFBiz3[OFBiz Legacy]
        DB5[(New DB)]
        DB6[(Legacy DB<br/>Read-Only)]
        
        Client3 --> Gateway3
        Gateway3 --> NewSvc3
        NewSvc3 --> DB5
        OFBiz3 -.->|Deprecated| DB6
    end
```

## Verwendung der Visualisierungen

Diese Diagramme können in verschiedenen Tools gerendert werden:

1. **GitHub/GitLab**: Unterstützen Mermaid nativ in Markdown
2. **VS Code**: Mit Mermaid-Extension
3. **Confluence**: Mit Mermaid-Plugin
4. **Online**: https://mermaid.live/

## Neo4j Cypher Queries für weitere Analysen

```cypher
// 1. Detaillierte Abhängigkeiten zwischen zwei Modulen
MATCH (source:Package)-[:CONTAINS*]->(st:Type)-[d:DEPENDS_ON]->(tt:Type)<-[:CONTAINS*]-(target:Package)
WHERE source.fqn STARTS WITH 'org.apache.ofbiz.order'
  AND target.fqn STARTS WITH 'org.apache.ofbiz.product'
RETURN st.name as orderClass, 
       tt.name as productClass,
       st.fqn as orderFQN,
       tt.fqn as productFQN
ORDER BY orderClass
LIMIT 50

// 2. Zentrale Klassen (Hub-Klassen)
MATCH (t:Type)
WHERE t.fqn STARTS WITH 'org.apache.ofbiz'
WITH t, 
     SIZE([(t)<-[:DEPENDS_ON]-() | 1]) as incomingDeps,
     SIZE([(t)-[:DEPENDS_ON]->() | 1]) as outgoingDeps
WHERE incomingDeps > 10
RETURN t.name, t.fqn, incomingDeps, outgoingDeps
ORDER BY incomingDeps DESC
LIMIT 20

// 3. Isolierte Module (wenig Abhängigkeiten)
MATCH (p:Package)
WHERE p.fqn =~ 'org\\.apache\\.ofbiz\\.[^.]+$'
WITH p.fqn as module
MATCH (source:Package)-[:CONTAINS*]->(st:Type)-[:DEPENDS_ON]->(tt:Type)<-[:CONTAINS*]-(target:Package)
WHERE source.fqn STARTS WITH module
  AND target.fqn STARTS WITH 'org.apache.ofbiz'
  AND NOT target.fqn STARTS WITH module
WITH module, COUNT(DISTINCT target) as externalDeps
RETURN module, externalDeps
ORDER BY externalDeps ASC

// 4. Shared Kernel Kandidaten
MATCH (t:Type)<-[:DEPENDS_ON]-(dependent:Type)<-[:CONTAINS*]-(p:Package)
WHERE p.fqn STARTS WITH 'org.apache.ofbiz'
  AND p.fqn =~ 'org\\.apache\\.ofbiz\\.(accounting|order|product|party|shipment).*'
WITH t, COLLECT(DISTINCT SPLIT(p.fqn, '.')[3]) as modules
WHERE SIZE(modules) >= 3
RETURN t.fqn, modules, SIZE(modules) as moduleCount
ORDER BY moduleCount DESC
LIMIT 30

// 5. Package-Größen und Komplexität
MATCH (p:Package)-[:CONTAINS*]->(t:Type)
WHERE p.fqn =~ 'org\\.apache\\.ofbiz\\.[^.]+$'
WITH p.fqn as module, COUNT(DISTINCT t) as typeCount
MATCH (p2:Package {fqn: module})-[:CONTAINS*]->(t2:Type)-[d:DEPENDS_ON]->()
WITH module, typeCount, COUNT(d) as totalDeps
RETURN module, typeCount, totalDeps, 
       toFloat(totalDeps) / typeCount as avgDepsPerType
ORDER BY avgDepsPerType DESC
```
