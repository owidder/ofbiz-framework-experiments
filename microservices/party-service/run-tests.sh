#!/bin/bash

# ============================================================================
# Party-Service E2E Test Runner
# ============================================================================
# Dieses Skript führt alle End-to-End Tests für den Party-Service aus.
#
# Verwendung:
#   ./run-tests.sh          # Startet alles und führt Tests aus
#   ./run-tests.sh --quick  # Nur Tests ausführen (Services müssen laufen)
# ============================================================================

set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8085"
BASE_PATH="/api/party"

echo ""
echo "============================================"
echo "  Party-Service E2E Tests"
echo "============================================"
echo ""

# Quick mode - nur Tests ausführen
if [ "$1" == "--quick" ]; then
    echo -e "${YELLOW}Quick Mode: Führe nur Tests aus...${NC}"
    echo ""

    # Prüfen ob Service läuft
    if curl -s "${BASE_URL}${BASE_PATH}/persons" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Party-Service läuft${NC}"
    else
        echo -e "${RED}✗ Party-Service nicht erreichbar!${NC}"
        echo "  Bitte starten mit: ./gradlew bootRun -Dspring.profiles.active=local"
        exit 1
    fi

    ./gradlew test --tests "PartyServiceIntegrationTest" -Dtest.baseUrl="${BASE_URL}" --no-daemon
    exit $?
fi

# Full mode - alles starten
echo "Schritt 1: Docker Container starten..."
docker-compose up -d
sleep 5

# Prüfen ob Container laufen
echo ""
echo "Schritt 2: Container-Status prüfen..."
if docker ps | grep -q party-kafka; then
    echo -e "${GREEN}✓ Kafka läuft${NC}"
else
    echo -e "${RED}✗ Kafka nicht gestartet!${NC}"
    exit 1
fi

if docker ps | grep -q party-service-db; then
    echo -e "${GREEN}✓ PostgreSQL läuft${NC}"
else
    echo -e "${RED}✗ PostgreSQL nicht gestartet!${NC}"
    exit 1
fi

# Party-Service starten (im Hintergrund)
echo ""
echo "Schritt 3: Party-Service starten..."

# Beende eventuell laufende Instanz
pkill -f "party-service" 2>/dev/null || true

./gradlew bootRun -Dspring.profiles.active=local --no-daemon &
SERVICE_PID=$!

# Warten bis Service bereit ist
echo "Warte auf Party-Service..."
for i in {1..30}; do
    if curl -s "${BASE_URL}${BASE_PATH}/persons" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Party-Service gestartet (Port 8085)${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}✗ Timeout beim Starten des Party-Service!${NC}"
        kill $SERVICE_PID 2>/dev/null
        exit 1
    fi
    sleep 2
done

# Tests ausführen
echo ""
echo "Schritt 4: Integration Tests ausführen..."
echo "============================================"
echo ""

./gradlew test --tests "PartyServiceIntegrationTest" -Dtest.baseUrl="${BASE_URL}" --no-daemon
TEST_RESULT=$?

# Service beenden
echo ""
echo "Schritt 5: Aufräumen..."
kill $SERVICE_PID 2>/dev/null || true

echo ""
if [ $TEST_RESULT -eq 0 ]; then
    echo -e "${GREEN}============================================${NC}"
    echo -e "${GREEN}  ALLE TESTS ERFOLGREICH!${NC}"
    echo -e "${GREEN}============================================${NC}"
else
    echo -e "${RED}============================================${NC}"
    echo -e "${RED}  TESTS FEHLGESCHLAGEN!${NC}"
    echo -e "${RED}============================================${NC}"
fi

exit $TEST_RESULT
