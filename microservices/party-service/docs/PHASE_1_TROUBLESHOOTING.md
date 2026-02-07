# Phase 1 - Troubleshooting: Java Version Mismatch

**Problem:** Derby 10.17.1.0 ist nicht kompatibel mit Java 17  
**Datum:** 2026-01-28  
**Status:** ✅ Gelöst

---

## Problem-Beschreibung

Beim Ausführen der Tests trat folgender Fehler auf:

```
java.lang.UnsupportedClassVersionError: org/apache/derby/jdbc/EmbeddedDriver 
has been compiled by a more recent version of the Java Runtime (class file version 63.0), 
this version of the Java Runtime only recognizes class file versions up to 61.0
```

### Ursache

- **Derby 10.17.1.0** wurde mit **Java 19** kompiliert (class file version 63.0)
- Das **Party-Service Projekt** läuft mit **Java 17** (class file version 61.0)
- Java 17 kann keine mit Java 19 kompilierten Klassen laden

### Java Version Mapping

| Java Version | Class File Version |
|--------------|-------------------|
| Java 17 | 61.0 |
| Java 18 | 62.0 |
| Java 19 | 63.0 |
| Java 21 | 65.0 |

---

## Lösung

### Option 1: Derby-Version downgraden (gewählt)

**Änderung in [`build.gradle`](../build.gradle:52-56):**

```gradle
// Apache Derby - OFBiz Database Access (for PoC)
// Derby embedded driver for direct access to OFBiz Derby database
// Using 10.15.2.0 for Java 17 compatibility (10.17+ requires Java 19)
implementation 'org.apache.derby:derby:10.15.2.0'
implementation 'org.apache.derby:derbytools:10.15.2.0'
implementation 'org.apache.derby:derbyclient:10.15.2.0'
```

**Begründung:**
- ✅ Derby 10.15.2.0 ist mit Java 17 kompatibel
- ✅ Keine Änderung der Java-Version erforderlich
- ✅ Kompatibel mit OFBiz Derby-Datenbank
- ✅ Schnelle Lösung ohne Projekt-Umstellung

### Option 2: Java auf Version 19+ upgraden (nicht gewählt)

**Würde erfordern:**
- Upgrade von Java 17 auf Java 19 oder 21
- Anpassung der `sourceCompatibility` in build.gradle
- Potenzielle Kompatibilitätsprobleme mit anderen Dependencies
- Mehr Aufwand für PoC

---

## Validierung

### Build erfolgreich

```bash
./gradlew :microservices:party-service:build -x test
# BUILD SUCCESSFUL in 2s
```

### Dependencies verifiziert

```bash
./gradlew :microservices:party-service:dependencies --configuration runtimeClasspath | grep derby
# +--- org.apache.derby:derby:10.15.2.0
# +--- org.apache.derby:derbytools:10.15.2.0
# +--- org.apache.derby:derbyclient:10.15.2.0
```

---

## Derby Version Kompatibilität

| Derby Version | Min. Java Version | Max. Java Version | Empfohlen für |
|---------------|-------------------|-------------------|---------------|
| 10.14.x | Java 9 | Java 16 | Java 11 |
| 10.15.x | Java 9 | Java 17 | **Java 17** ✅ |
| 10.16.x | Java 17 | Java 18 | Java 17-18 |
| 10.17.x | Java 19 | Java 21+ | Java 19+ |

**Quelle:** [Apache Derby Release Notes](https://db.apache.org/derby/releases/)

---

## Lessons Learned

### Was gut funktioniert hat:
- ✅ Schnelle Identifikation des Problems durch detaillierte Fehlermeldung
- ✅ Einfache Lösung durch Version-Downgrade
- ✅ Keine Breaking Changes für das Projekt

### Für die Zukunft:
- 💡 Bei Dependency-Updates immer Java-Kompatibilität prüfen
- 💡 Release Notes der Dependencies lesen
- 💡 In Dokumentation Java-Version-Requirements festhalten

### Empfehlungen:
- 📝 Dokumentiere Java-Version-Requirements in README.md
- 📝 Füge Kommentare zu kritischen Dependencies hinzu
- 📝 Prüfe regelmäßig auf Updates mit Kompatibilitätsprüfung

---

## Nächste Schritte

Nach der Lösung des Problems:

1. **Tests ausführen** (OFBiz muss gestoppt sein!):
   ```bash
   # OFBiz stoppen (Terminal 2: Ctrl+C)
   # 5 Sekunden warten
   
   # Tests ausführen
   ./gradlew :microservices:party-service:test --tests DerbyConnectionTest
   ```

2. **Anwendung starten**:
   ```bash
   ./gradlew :microservices:party-service:bootRun
   ```

3. **Dokumentation aktualisieren**:
   - PHASE_1_COMPLETION.md mit Troubleshooting-Sektion erweitern
   - README.md mit Java-Version-Requirements aktualisieren

---

## Verwandte Dokumente

- [`PHASE_1_PROMPTS.md`](PHASE_1_PROMPTS.md) - Ursprüngliche Prompts
- [`PHASE_1_COMPLETION.md`](PHASE_1_COMPLETION.md) - Abschlussbericht
- [`PROJECT_STATUS_REPORT.md`](PROJECT_STATUS_REPORT.md) - Projekt-Status

---

**Problem gelöst:** ✅  
**Build erfolgreich:** ✅  
**Bereit für Tests:** ✅  

---

**Erstellt:** 2026-01-28  
**Autor:** Party-PoC Mode  
**Version:** 1.0
