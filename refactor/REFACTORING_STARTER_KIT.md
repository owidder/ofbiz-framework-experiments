# OFBiz Refaktorierungs-Starter-Kit

## Schnelleinstieg für die erste Refaktorierung

Dieses Dokument enthält praktische Code-Templates und Checklisten für den Start der Refaktorierung.

---

## 1. Projekt-Setup

### 1.1 Maven/Gradle Dependencies hinzufügen

#### Spring Framework (für Dependency Injection)
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>6.0.0</version>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
    <version>6.0.0</version>
</dependency>
```

#### Event-Driven Architecture
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context-support</artifactId>
    <version>6.0.0</version>
</dependency>
```

#### Testing
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <version>3.0.0</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.17.0</version>
    <scope>test</scope>
</dependency>
```

---

## 2. Code-Templates

### 2.1 Service-Interface Template

```java
package org.apache.ofbiz.order.service;

import java.util.Map;

/**
 * Service-Interface für Order-Verwaltung
 * 
 * Diese Schnittstelle definiert die öffentliche API für Order-Operationen.
 * Implementierungen sollten diese Schnittstelle implementieren.
 * 
 * @author OFBiz Refactoring Team
 * @version 1.0
 */
public interface OrderService {
    
    /**
     * Erstellt eine neue Bestellung
     * 
     * @param orderData Map mit Bestelldaten
     *                  - orderId: String (optional, wird generiert wenn nicht vorhanden)
     *                  - customerId: String (erforderlich)
     *                  - orderDate: Timestamp (optional, wird auf jetzt gesetzt)
     *                  - items: List<Map> (erforderlich)
     * 
     * @return Map mit Ergebnis
     *         - success: boolean
     *         - orderId: String (wenn erfolgreich)
     *         - message: String (Fehlermeldung wenn nicht erfolgreich)
     * 
     * @throws OrderServiceException wenn ein Fehler auftritt
     */
    Map<String, Object> createOrder(Map<String, Object> orderData) 
        throws OrderServiceException;
    
    /**
     * Aktualisiert den Status einer Bestellung
     * 
     * @param orderId Die ID der Bestellung
     * @param newStatus Der neue Status
     * 
     * @return Map mit Ergebnis
     * 
     * @throws OrderServiceException wenn Bestellung nicht gefunden
     */
    Map<String, Object> updateOrderStatus(String orderId, String newStatus) 
        throws OrderServiceException;
    
    /**
     * Ruft eine Bestellung ab
     * 
     * @param orderId Die ID der Bestellung
     * 
     * @return Map mit Bestelldaten
     * 
     * @throws OrderServiceException wenn Bestellung nicht gefunden
     */
    Map<String, Object> getOrder(String orderId) 
        throws OrderServiceException;
    
    /**
     * Storniert eine Bestellung
     * 
     * @param orderId Die ID der Bestellung
     * @param reason Der Grund für die Stornierung
     * 
     * @return Map mit Ergebnis
     * 
     * @throws OrderServiceException wenn Bestellung nicht storniert werden kann
     */
    Map<String, Object> cancelOrder(String orderId, String reason) 
        throws OrderServiceException;
}
```

### 2.2 Service-Implementierung Template

```java
package org.apache.ofbiz.order.service.impl;

import org.apache.ofbiz.order.service.OrderService;
import org.apache.ofbiz.order.service.OrderServiceException;
import org.apache.ofbiz.payment.service.PaymentService;
import org.apache.ofbiz.shipping.service.ShippingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementierung des OrderService
 * 
 * Diese Klasse implementiert die Order-Verwaltungslogik.
 * Sie nutzt Dependency Injection für externe Services.
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final OrderRepository orderRepository;
    private final OrderValidator orderValidator;
    
    /**
     * Constructor mit Dependency Injection
     */
    public OrderServiceImpl(
            PaymentService paymentService,
            ShippingService shippingService,
            OrderRepository orderRepository,
            OrderValidator orderValidator) {
        this.paymentService = paymentService;
        this.shippingService = shippingService;
        this.orderRepository = orderRepository;
        this.orderValidator = orderValidator;
    }
    
    @Override
    public Map<String, Object> createOrder(Map<String, Object> orderData) 
            throws OrderServiceException {
        
        logger.info("Creating new order with data: {}", orderData);
        
        try {
            // Validierung
            Map<String, String> validationErrors = orderValidator.validate(orderData);
            if (!validationErrors.isEmpty()) {
                logger.warn("Order validation failed: {}", validationErrors);
                return createErrorResponse("Validation failed", validationErrors);
            }
            
            // Order-ID generieren wenn nicht vorhanden
            String orderId = (String) orderData.getOrDefault("orderId", 
                UUID.randomUUID().toString());
            
            // Order erstellen
            Order order = new Order();
            order.setOrderId(orderId);
            order.setCustomerId((String) orderData.get("customerId"));
            order.setOrderDate(new Date());
            order.setStatus("PENDING");
            
            // In Datenbank speichern
            Order savedOrder = orderRepository.save(order);
            
            logger.info("Order created successfully: {}", orderId);
            
            // Event publishen (für asynchrone Verarbeitung)
            publishOrderCreatedEvent(savedOrder);
            
            return createSuccessResponse("Order created", orderId);
            
        } catch (Exception e) {
            logger.error("Error creating order", e);
            throw new OrderServiceException("Failed to create order", e);
        }
    }
    
    @Override
    public Map<String, Object> updateOrderStatus(String orderId, String newStatus) 
            throws OrderServiceException {
        
        logger.info("Updating order {} status to {}", orderId, newStatus);
        
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderServiceException("Order not found: " + orderId));
            
            String oldStatus = order.getStatus();
            order.setStatus(newStatus);
            
            Order updatedOrder = orderRepository.save(order);
            
            logger.info("Order status updated from {} to {}", oldStatus, newStatus);
            
            // Event publishen
            publishOrderStatusChangedEvent(updatedOrder, oldStatus);
            
            return createSuccessResponse("Order status updated", orderId);
            
        } catch (Exception e) {
            logger.error("Error updating order status", e);
            throw new OrderServiceException("Failed to update order status", e);
        }
    }
    
    @Override
    public Map<String, Object> getOrder(String orderId) 
            throws OrderServiceException {
        
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderServiceException("Order not found: " + orderId));
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("order", order);
            return result;
            
        } catch (Exception e) {
            logger.error("Error retrieving order", e);
            throw new OrderServiceException("Failed to retrieve order", e);
        }
    }
    
    @Override
    public Map<String, Object> cancelOrder(String orderId, String reason) 
            throws OrderServiceException {
        
        logger.info("Cancelling order {} with reason: {}", orderId, reason);
        
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderServiceException("Order not found: " + orderId));
            
            if ("COMPLETED".equals(order.getStatus())) {
                throw new OrderServiceException("Cannot cancel completed order");
            }
            
            order.setStatus("CANCELLED");
            order.setCancellationReason(reason);
            
            Order cancelledOrder = orderRepository.save(order);
            
            logger.info("Order cancelled successfully");
            
            // Event publishen
            publishOrderCancelledEvent(cancelledOrder);
            
            return createSuccessResponse("Order cancelled", orderId);
            
        } catch (Exception e) {
            logger.error("Error cancelling order", e);
            throw new OrderServiceException("Failed to cancel order", e);
        }
    }
    
    // Helper-Methoden
    
    private void publishOrderCreatedEvent(Order order) {
        // Event-Publishing-Logik
        logger.debug("Publishing OrderCreatedEvent for order: {}", order.getOrderId());
    }
    
    private void publishOrderStatusChangedEvent(Order order, String oldStatus) {
        // Event-Publishing-Logik
        logger.debug("Publishing OrderStatusChangedEvent for order: {}", order.getOrderId());
    }
    
    private void publishOrderCancelledEvent(Order order) {
        // Event-Publishing-Logik
        logger.debug("Publishing OrderCancelledEvent for order: {}", order.getOrderId());
    }
    
    private Map<String, Object> createSuccessResponse(String message, String orderId) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("orderId", orderId);
        return response;
    }
    
    private Map<String, Object> createErrorResponse(String message, Map<String, String> errors) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("errors", errors);
        return response;
    }
}
```

### 2.3 Exception-Klasse Template

```java
package org.apache.ofbiz.order.service;

/**
 * Exception für Order-Service-Fehler
 */
public class OrderServiceException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public OrderServiceException(String message) {
        super(message);
    }
    
    public OrderServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public OrderServiceException(Throwable cause) {
        super(cause);
    }
}
```

### 2.4 Unit-Test Template

```java
package org.apache.ofbiz.order.service.impl;

import org.apache.ofbiz.order.service.OrderService;
import org.apache.ofbiz.order.service.OrderServiceException;
import org.apache.ofbiz.payment.service.PaymentService;
import org.apache.ofbiz.shipping.service.ShippingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für OrderServiceImpl
 */
@DisplayName("OrderService Tests")
class OrderServiceImplTest {
    
    private OrderService orderService;
    
    @Mock
    private PaymentService paymentService;
    
    @Mock
    private ShippingService shippingService;
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderValidator orderValidator;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderServiceImpl(
            paymentService,
            shippingService,
            orderRepository,
            orderValidator
        );
    }
    
    @Test
    @DisplayName("Should create order successfully")
    void testCreateOrderSuccess() throws OrderServiceException {
        // Arrange
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("customerId", "CUST123");
        orderData.put("items", new HashMap<>());
        
        when(orderValidator.validate(orderData)).thenReturn(new HashMap<>());
        
        // Act
        Map<String, Object> result = orderService.createOrder(orderData);
        
        // Assert
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("orderId"));
        verify(orderRepository, times(1)).save(any());
    }
    
    @Test
    @DisplayName("Should fail when validation fails")
    void testCreateOrderValidationFails() throws OrderServiceException {
        // Arrange
        Map<String, Object> orderData = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        errors.put("customerId", "Customer ID is required");
        
        when(orderValidator.validate(orderData)).thenReturn(errors);
        
        // Act
        Map<String, Object> result = orderService.createOrder(orderData);
        
        // Assert
        assertFalse((Boolean) result.get("success"));
        verify(orderRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should update order status")
    void testUpdateOrderStatus() throws OrderServiceException {
        // Arrange
        String orderId = "ORD123";
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus("PENDING");
        
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        
        // Act
        Map<String, Object> result = orderService.updateOrderStatus(orderId, "APPROVED");
        
        // Assert
        assertTrue((Boolean) result.get("success"));
        verify(orderRepository, times(1)).save(any());
    }
    
    @Test
    @DisplayName("Should throw exception when order not found")
    void testUpdateOrderStatusNotFound() {
        // Arrange
        String orderId = "NONEXISTENT";
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.empty());
        
        // Act & Assert
        assertThrows(OrderServiceException.class, 
            () -> orderService.updateOrderStatus(orderId, "APPROVED"));
    }
}
```

### 2.5 Integration-Test Template

```java
package org.apache.ofbiz.order.service.impl;

import org.apache.ofbiz.order.service.OrderService;
import org.apache.ofbiz.order.service.OrderServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-Tests für OrderService
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OrderService Integration Tests")
class OrderServiceIntegrationTest {
    
    @Autowired
    private OrderService orderService;
    
    @Test
    @DisplayName("Should create and retrieve order")
    void testCreateAndRetrieveOrder() throws OrderServiceException {
        // Arrange
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("customerId", "CUST123");
        orderData.put("items", new HashMap<>());
        
        // Act
        Map<String, Object> createResult = orderService.createOrder(orderData);
        String orderId = (String) createResult.get("orderId");
        
        Map<String, Object> getResult = orderService.getOrder(orderId);
        
        // Assert
        assertTrue((Boolean) createResult.get("success"));
        assertTrue((Boolean) getResult.get("success"));
        assertNotNull(getResult.get("order"));
    }
    
    @Test
    @DisplayName("Should update order status through lifecycle")
    void testOrderLifecycle() throws OrderServiceException {
        // Arrange
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("customerId", "CUST123");
        orderData.put("items", new HashMap<>());
        
        // Act - Create
        Map<String, Object> createResult = orderService.createOrder(orderData);
        String orderId = (String) createResult.get("orderId");
        
        // Act - Update to APPROVED
        Map<String, Object> approveResult = orderService.updateOrderStatus(orderId, "APPROVED");
        
        // Act - Update to SHIPPED
        Map<String, Object> shipResult = orderService.updateOrderStatus(orderId, "SHIPPED");
        
        // Assert
        assertTrue((Boolean) createResult.get("success"));
        assertTrue((Boolean) approveResult.get("success"));
        assertTrue((Boolean) shipResult.get("success"));
    }
}
```

---

## 3. Konfiguration

### 3.1 Spring Configuration

```java
package org.apache.ofbiz.order.config;

import org.apache.ofbiz.order.service.OrderService;
import org.apache.ofbiz.order.service.impl.OrderServiceImpl;
import org.apache.ofbiz.payment.service.PaymentService;
import org.apache.ofbiz.shipping.service.ShippingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring-Konfiguration für Order-Services
 */
@Configuration
public class OrderServiceConfiguration {
    
    @Bean
    public OrderService orderService(
            PaymentService paymentService,
            ShippingService shippingService,
            OrderRepository orderRepository,
            OrderValidator orderValidator) {
        return new OrderServiceImpl(
            paymentService,
            shippingService,
            orderRepository,
            orderValidator
        );
    }
    
    @Bean
    public OrderValidator orderValidator() {
        return new OrderValidator();
    }
}
```

---

## 4. Migrations-Checkliste

### Phase 1: Vorbereitung
- [ ] Backup der aktuellen Codebase erstellen
- [ ] Feature-Branch erstellen: `feature/refactor-order-service`
- [ ] Dependencies in pom.xml hinzufügen
- [ ] Spring-Konfiguration erstellen

### Phase 2: Interface & Implementierung
- [ ] OrderService-Interface erstellen
- [ ] OrderServiceImpl implementieren
- [ ] OrderServiceException erstellen
- [ ] OrderValidator implementieren
- [ ] OrderRepository erstellen

### Phase 3: Tests
- [ ] Unit-Tests schreiben (Ziel: >80% Coverage)
- [ ] Integration-Tests schreiben
- [ ] Alle Tests grün machen
- [ ] Performance-Tests durchführen

### Phase 4: Migration
- [ ] Alle Aufrufer von altem OrderServices identifizieren
- [ ] Aufrufer schrittweise migrieren
- [ ] Alte Implementierung als deprecated markieren
- [ ] Alte Implementierung entfernen (nach Übergangsfrist)

### Phase 5: Validierung
- [ ] Funktionale Tests durchführen
- [ ] Performance-Vergleich durchführen
- [ ] Security-Review durchführen
- [ ] Code-Review durchführen

### Phase 6: Deployment
- [ ] In Staging deployen
- [ ] Smoke-Tests durchführen
- [ ] In Production deployen
- [ ] Monitoring aktivieren

---

## 5. Häufige Probleme & Lösungen

### Problem: Zirkuläre Abhängigkeiten bei Dependency Injection

**Symptom:** `BeanCurrentlyInCreationException`

**Lösung:**
```java
// Verwenden Sie Lazy-Initialization
@Autowired
private ObjectProvider<PaymentService> paymentServiceProvider;

// Oder verwenden Sie Setter-Injection statt Constructor-Injection
@Autowired
public void setPaymentService(PaymentService paymentService) {
    this.paymentService = paymentService;
}
```

### Problem: Tests schlagen fehl wegen fehlender Beans

**Symptom:** `NoSuchBeanDefinitionException`

**Lösung:**
```java
// Verwenden Sie @MockBean für externe Services
@SpringBootTest
class OrderServiceTest {
    @MockBean
    private PaymentService paymentService;
    
    @Autowired
    private OrderService orderService;
}
```

### Problem: Performance-Degradation nach Refaktorierung

**Symptom:** Queries sind langsamer

**Lösung:**
```java
// Verwenden Sie @Transactional(readOnly = true) für Read-Operationen
@Transactional(readOnly = true)
public Map<String, Object> getOrder(String orderId) {
    // ...
}

// Verwenden Sie Lazy-Loading für Beziehungen
@OneToMany(fetch = FetchType.LAZY)
private List<OrderItem> items;
```

---

## 6. Monitoring & Logging

### 6.1 Logging-Konfiguration

```xml
<!-- logback.xml -->
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/order-service.log</file>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="org.apache.ofbiz.order.service" level="DEBUG"/>
    
    <root level="INFO">
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### 6.2 Metriken mit Micrometer

```java
@Service
public class OrderServiceImpl implements OrderService {
    
    private final MeterRegistry meterRegistry;
    
    public OrderServiceImpl(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    @Override
    public Map<String, Object> createOrder(Map<String, Object> orderData) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            // Order-Erstellung
            meterRegistry.counter("orders.created").increment();
            return result;
        } catch (Exception e) {
            meterRegistry.counter("orders.creation.failed").increment();
            throw e;
        } finally {
            sample.stop(Timer.builder("order.creation.time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry));
        }
    }
}
```

---

## 7. Nächste Schritte

1. **Diese Woche:**
   - [ ] Spring-Dependencies hinzufügen
   - [ ] OrderService-Interface erstellen
   - [ ] OrderServiceImpl implementieren

2. **Nächste Woche:**
   - [ ] Unit-Tests schreiben
   - [ ] Integration-Tests schreiben
   - [ ] Code-Review durchführen

3. **Folgende Woche:**
   - [ ] Aufrufer migrieren
   - [ ] In Staging deployen
   - [ ] Performance-Tests durchführen

---

## Ressourcen

- [Spring Framework Documentation](https://spring.io/projects/spring-framework)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [OFBiz Developer Guide](https://ofbiz.apache.org/developers.html)

---

## Support

Bei Fragen oder Problemen:
1. Konsultieren Sie die Neo4j-Analyse
2. Führen Sie zusätzliche Cypher-Queries durch
3. Dokumentieren Sie Erkenntnisse
4. Aktualisieren Sie diesen Leitfaden
