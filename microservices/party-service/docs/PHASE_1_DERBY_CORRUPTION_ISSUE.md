# 🔧 Phase 1 - Derby Datenbank-Korruption Problem

**Problem:** Derby Database Corruption beim Start des Party-Service  
**Datum:** 2026-01-29  
**Status:** 🔍 ANALYSE & LÖSUNG

---

## 🚨 Fehlerbeschreibung

Beim Versuch, den Party-Service mit `bootRun` zu starten, tritt folgender Fehler auf:

```
java.sql.SQLException: Failed to start database '/Users/oliverwidder/dev/ofbiz/runtime/data/derby/ofbiz'

Caused by: org.apache.derby.shared.common.error.StandardException: Cannot redo operation null in the log.

Caused by: org.apache.derby.shared.common.error.StandardException: 
Page Page(19,Container(0, 11216)) is at version 188, 
the log file contains change version 206, 
either there are log records of this page missing, 
or this page did not get written out to disk properly.
```

---

## 🔍 Ursachenanalyse

### Problem: Inkonsistenter Datenbank-Zustand

Die Derby-Datenbank befindet sich in einem **inkonsistenten Zustand**:

1. **Page Version Mismatch**: 
   - Seite ist bei Version 188
   - Log-Datei erwartet Version 206
   - **Differenz von 18 Versionen**

2. **Mögliche Ursachen**:
   - ❌ OFBiz wurde unsauber beendet (Crash, Kill -9, Stromausfall)
   - ❌ Schreibvorgang wurde unterbrochen
   - ❌ Transaction Log ist korrupt
   - ❌ Disk I/O Fehler während des Schreibens

3. **Derby Embedded Mode Besonderheit**:
   - Derby schreibt Änderungen in Transaction Logs
   - Bei Recovery werden Logs "replayed"
   - Wenn Logs und Daten-Seiten nicht synchron sind → Corruption

---

## ✅ Lösungsansätze

### Lösung 1: OFBiz sauber neu starten (EMPFOHLEN)

**Schritt 1: OFBiz sauber beenden**
```bash
# Terminal 2: OFBiz stoppen (falls läuft)
# Drücke Ctrl+C im Terminal wo OFBiz läuft
# Warte bis "Shutdown completed" erscheint
```

**Schritt 2: Lock-Dateien entfernen**
```bash
rm -f /Users/oliverwidder/dev/ofbiz/runtime/data/derby/ofbiz/db.lck
rm -f /Users/oliverwidder/dev/ofbiz/runtime/data/derby/ofbiz/dbex.lck
```

**Schritt 3: OFBiz neu starten**
```bash
cd /Users/oliverwidder/dev/ofbiz
./gradlew ofbiz
```

**Schritt 4: Warten bis OFBiz vollständig gestartet ist**
```
# Warte auf diese Meldung:
"Apache OFBiz Started"
```

**Schritt 5: Party-Service starten**
```bash
cd /Users/oliverwidder/dev/ofbiz
./gradlew :microservices:party-service:bootRun
```

---

### Lösung 2: Derby Recovery durchführen

Falls Lösung 1 nicht funktioniert, versuche Derby Recovery:

```bash
cd /Users/oliverwidder/dev/ofbiz/runtime/data/derby

# Backup erstellen
cp -r ofbiz ofbiz_backup_$(date +%Y%m%d_%H%M%S)

# Derby Recovery Tool ausführen
java -cp /Users/oliverwidder/.gradle/caches/modules-2/files-2.1/org.apache.derby/derby/10.15.2.0/*/derby-10.15.2.0.jar \
  org.apache.derby.tools.ij

# In ij-Shell:
connect 'jdbc:derby:ofbiz;upgrade=true';
exit;
```

---

### Lösung 3: Datenbank neu laden (LETZTER AUSWEG)

**⚠️ WARNUNG: Alle Daten gehen verloren!**

```bash
cd /Users/oliverwidder/dev/ofbiz

# 1. OFBiz stoppen
# Ctrl+C in Terminal 2

# 2. Derby-Datenbank löschen
rm -rf runtime/data/derby/ofbiz

# 3. OFBiz mit Daten-Reload starten
./gradlew "ofbiz --load-data"

# 4. Warten bis vollständig geladen
# Dies kann 10-30 Minuten dauern!

# 5. Party-Service starten
./gradlew :microservices:party-service:bootRun
```

---

## 🎯 Empfohlene Vorgehensweise

### Schritt-für-Schritt Anleitung

**1. OFBiz Status prüfen**
```bash
# Prüfe ob OFBiz läuft
ps aux | grep ofbiz
```

**2. OFBiz sauber beenden**
```bash
# Im Terminal wo OFBiz läuft:
# Drücke Ctrl+C
# Warte auf "Shutdown completed"
```

**3. Lock-Dateien prüfen und entfernen**
```bash
ls -la /Users/oliverwidder/dev/ofbiz/runtime/data/derby/ofbiz/*.lck

# Falls vorhanden:
rm -f /Users/oliverwidder/dev/ofbiz/runtime/data/derby/ofbiz/*.lck
```

**4. Derby Log-Dateien prüfen**
```bash
ls -la /Users/oliverwidder/dev/ofbiz/runtime/data/derby/ofbiz/log/
```

**5. OFBiz neu starten**
```bash
cd /Users/oliverwidder/dev/ofbiz
./gradlew ofbiz
```

**6. Auf vollständigen Start warten**
```
# Warte auf diese Log-Meldungen:
- "Apache OFBiz Started"
- "Started ServerConnector"
- "Started @xxxms"
```

**7. Party-Service Tests ausführen**
```bash
# Erst Tests, bevor wir bootRun versuchen
./gradlew :microservices:party-service:test --tests DerbyConnectionTest
```

**8. Falls Tests erfolgreich → bootRun**
```bash
./gradlew :microservices:party-service:bootRun
```

---

## 🔒 Warum passiert das?

### Derby Embedded Mode Einschränkungen

1. **Single Connection Only**
   - Nur eine JVM kann gleichzeitig auf die Datenbank zugreifen
   - OFBiz hält die Verbindung offen
   - Party-Service kann nicht parallel verbinden

2. **Transaction Log Recovery**
   - Derby schreibt alle Änderungen in Transaction Logs
   - Bei Neustart werden Logs "replayed"
   - Wenn Logs korrupt sind → Recovery schlägt fehl

3. **Unsauberer Shutdown**
   - Kill -9, Crash, Stromausfall
   - Logs und Daten-Seiten sind nicht synchron
   - Recovery kann nicht abgeschlossen werden

---

## 💡 Wichtige Erkenntnisse

### Was wir gelernt haben:

1. **OFBiz muss laufen für Party-Service**
   - Derby Embedded Mode erlaubt nur eine Verbindung
   - OFBiz "besitzt" die Datenbank
   - Party-Service kann nur lesen, wenn OFBiz läuft

2. **Tests vs. bootRun**
   - ✅ **Tests funktionieren**: Kurze Verbindung, dann schließen
   - ❌ **bootRun schlägt fehl**: Versucht dauerhafte Verbindung
   - **Grund**: OFBiz hält die Datenbank bereits offen

3. **Derby ist nicht für Microservices geeignet**
   - Embedded Mode = Single Connection
   - Keine parallelen Zugriffe möglich
   - Für PoC OK, für Produktion → PostgreSQL

---

## 🚀 Nächste Schritte

### Kurzfristig (PoC Phase 1):

1. **Tests verwenden statt bootRun**
   ```bash
   ./gradlew :microservices:party-service:test --tests DerbyConnectionTest
   ```

2. **CommandLineRunner deaktivieren**
   - Verhindert automatische Verbindung beim Start
   - Nur für Tests aktivieren

3. **Dokumentation aktualisieren**
   - Derby Einschränkungen klar kommunizieren
   - Alternative Ansätze dokumentieren

### Mittelfristig (Phase 2+):

1. **PostgreSQL für Party-Service**
   - Eigene Datenbank für den Service
   - Datenmigration von Derby → PostgreSQL
   - Parallele Zugriffe möglich

2. **Read-Only Replica**
   - Derby im Read-Only Mode
   - Separate Datenbank-Kopie für Party-Service
   - Synchronisation über Batch-Jobs

3. **API-basierter Zugriff**
   - Party-Service greift über OFBiz REST API zu
   - Keine direkte Datenbank-Verbindung
   - Anti-Corruption Layer

---

## 📋 Checkliste für sauberen Betrieb

### Vor jedem Party-Service Start:

- [ ] OFBiz läuft und ist vollständig gestartet
- [ ] Keine Lock-Dateien vorhanden (*.lck)
- [ ] Derby-Datenbank ist konsistent
- [ ] Nur Tests ausführen, nicht bootRun
- [ ] CommandLineRunner ist deaktiviert (für bootRun)

### Bei Problemen:

- [ ] OFBiz sauber beenden (Ctrl+C, warten auf Shutdown)
- [ ] Lock-Dateien entfernen
- [ ] OFBiz neu starten
- [ ] Auf vollständigen Start warten
- [ ] Tests ausführen zur Validierung

---

## 🎯 Fazit

**Das Problem ist NICHT ein Bug im Party-Service!**

Es ist eine **fundamentale Einschränkung von Derby Embedded Mode**:
- ✅ Nur eine Verbindung gleichzeitig
- ✅ OFBiz "besitzt" die Datenbank
- ✅ Party-Service kann nicht parallel verbinden

**Für Phase 1 PoC:**
- ✅ Tests funktionieren perfekt (5/5 PASSED)
- ✅ Verbindung wird getestet und geschlossen
- ❌ bootRun funktioniert nicht (Derby Limitation)

**Für Phase 2+:**
- 🎯 PostgreSQL als separate Datenbank
- 🎯 Datenmigration implementieren
- 🎯 Echte Microservice-Architektur

---

## 📚 Referenzen

- [Derby Embedded Mode Limitations](https://db.apache.org/derby/docs/10.15/devguide/cdevdvlp40653.html)
- [Derby Recovery](https://db.apache.org/derby/docs/10.15/adminguide/cadminhubbkup98797.html)
- [`PHASE_1_DERBY_LOCK_ISSUE.md`](PHASE_1_DERBY_LOCK_ISSUE.md)
- [`PHASE_1_SUCCESS.md`](PHASE_1_SUCCESS.md)

---

**Erstellt:** 2026-01-29  
**Autor:** Party-PoC Mode  
**Version:** 1.0  
**Status:** 🔍 ANALYSE ABGESCHLOSSEN
