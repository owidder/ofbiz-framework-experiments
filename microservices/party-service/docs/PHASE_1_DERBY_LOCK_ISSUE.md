# Phase 1 - Wichtiger Hinweis: OFBiz muss gestoppt werden!

**Problem:** Tests schlagen fehl, weil OFBiz noch läuft  
**Datum:** 2026-01-28  
**Status:** ⚠️ Aktion erforderlich

---

## Das Problem

Derby Embedded Mode erlaubt **nur eine Verbindung** zur Datenbank. Wenn OFBiz läuft, ist die Datenbank gesperrt.

### Fehlermeldung

```
ERROR XSDB6: Another instance of Derby may have already booted the database 
/Users/oliverwidder/dev/ofbiz/runtime/data/derby/ofbiz
```

### Ursache

In **Terminal 2** läuft OFBiz noch:
```
2026-01-28 19:12:05,754 |OFBiz-JobQueue-1 |PersistedServiceJob |I| Job [Auto-create Fixed Asset Maintenances]
```

---

## Lösung: OFBiz stoppen

### Schritt-für-Schritt Anleitung

1. **Terminal 2 aktivieren** (wo OFBiz läuft)

2. **OFBiz stoppen:**
   ```
   Drücke: Ctrl+C
   ```

3. **Warten** (5-10 Sekunden)
   - Derby muss die Datenbank-Locks freigeben
   - Warte bis keine Logs mehr erscheinen

4. **Bestätigung:**
   ```
   Du solltest sehen:
   "Shutdown completed"
   oder ähnliche Meldung
   ```

5. **Jetzt Tests ausführen:**
   ```bash
   cd /Users/oliverwidder/dev/ofbiz
   ./gradlew :microservices:party-service:test --tests DerbyConnectionTest
   ```

---

## Warum ist das notwendig?

### Derby Embedded Mode Einschränkungen

| Aspekt | Embedded Mode | Network Mode |
|--------|---------------|--------------|
| Verbindungen | **Nur 1** | Mehrere |
| Performance | Schneller | Langsamer |
| Setup | Einfach | Komplex |
| Für PoC | ✅ Geeignet | ❌ Overkill |

**Für den PoC:** Embedded Mode ist ausreichend, aber OFBiz und Party-Service können **nicht gleichzeitig** laufen.

---

## Alternative: Derby Network Server (Optional)

Falls du OFBiz und Party-Service gleichzeitig laufen lassen möchtest:

### 1. Derby Network Server starten

```bash
cd /Users/oliverwidder/dev/ofbiz/runtime/data/derby
java -jar $DERBY_HOME/lib/derbyrun.jar server start
```

### 2. application-dev.yml anpassen

```yaml
spring:
  datasource:
    url: jdbc:derby://localhost:1527/ofbiz
    driver-class-name: org.apache.derby.jdbc.ClientDriver
```

### 3. OFBiz für Network Mode konfigurieren

In `framework/entity/config/entityengine.xml`:
```xml
<datasource name="localderby"
    helper-class="org.apache.ofbiz.entity.datasource.GenericHelperDAO"
    field-type-name="derby"
    check-on-start="true"
    add-missing-on-start="true"
    use-pk-constraint-names="false"
    use-indices-unique="false"
    alias-view-columns="false">
    <read-data reader-name="seed"/>
    <read-data reader-name="seed-initial"/>
    <read-data reader-name="demo"/>
    <read-data reader-name="ext"/>
    <inline-jdbc
        jdbc-driver="org.apache.derby.jdbc.ClientDriver"
        jdbc-uri="jdbc:derby://localhost:1527/ofbiz"
        jdbc-username="ofbiz"
        jdbc-password="ofbiz"
        isolation-level="ReadCommitted"
        pool-minsize="2"
        pool-maxsize="250"
        time-between-eviction-runs-millis="600000"/>
</datasource>
```

**Aber:** Für den PoC ist das **nicht notwendig**!

---

## Workflow für PoC

### Entwicklung mit OFBiz

```bash
# Terminal 2: OFBiz läuft
./gradlew ofbiz

# Arbeite mit OFBiz UI
# Teste Funktionen
# Prüfe Daten
```

### Tests mit Party-Service

```bash
# Terminal 2: OFBiz stoppen
Ctrl+C

# Warte 5 Sekunden

# Tests ausführen
./gradlew :microservices:party-service:test --tests DerbyConnectionTest

# Oder Anwendung starten
./gradlew :microservices:party-service:bootRun
```

### Zurück zu OFBiz

```bash
# Party-Service stoppen
Ctrl+C

# Warte 5 Sekunden

# OFBiz starten
./gradlew ofbiz
```

---

## Checkliste vor Tests

- [ ] Terminal 2 prüfen: Läuft OFBiz?
- [ ] Falls ja: OFBiz stoppen (Ctrl+C)
- [ ] 5-10 Sekunden warten
- [ ] Keine Derby-Logs mehr sichtbar
- [ ] Jetzt Tests ausführen

---

## Erwartete Test-Ausgabe

Nach dem Stoppen von OFBiz sollten die Tests erfolgreich sein:

```
DerbyConnectionTest > shouldConnectToDerbyDatabase() PASSED
DerbyConnectionTest > shouldQuerySampleParties() PASSED
DerbyConnectionTest > shouldQueryPersonData() PASSED
DerbyConnectionTest > shouldQueryPartyGroupData() PASSED
DerbyConnectionTest > shouldListPartyTables() PASSED

BUILD SUCCESSFUL
5 tests completed, 5 passed
```

---

## Zusammenfassung

**Problem:** Derby Embedded Mode = nur 1 Verbindung  
**Lösung:** OFBiz stoppen vor Tests  
**Workflow:** Abwechselnd OFBiz oder Party-Service  
**Für Produktion:** PostgreSQL oder Derby Network Mode  

---

**Erstellt:** 2026-01-28  
**Autor:** Party-PoC Mode  
**Version:** 1.0
