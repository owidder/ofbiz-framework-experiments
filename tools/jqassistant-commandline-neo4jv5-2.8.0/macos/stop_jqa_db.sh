#!/bin/bash
# Stop jqAssistant Neo4j Database Server
# Usage: ./stop_jqa_db.sh

set -e

PID_FILE="/tmp/jqassistant-server.pid"
INPUT_FIFO="/tmp/jqassistant-input"

echo "=========================================="
echo "Stopping jqAssistant Neo4j Database"
echo "=========================================="
echo ""

# Check if PID file exists
if [ ! -f "$PID_FILE" ]; then
    echo "✓ Server is not running (no PID file found)"
    exit 0
fi

# Get PID
SERVER_PID=$(cat "$PID_FILE")

# Check if process is running
if ! ps -p "$SERVER_PID" > /dev/null 2>&1; then
    echo "✓ Server is not running (PID $SERVER_PID not found)"
    rm -f "$PID_FILE"
    exit 0
fi

# Try graceful shutdown first
echo "Stopping server (PID: $SERVER_PID)..."

# Try to write to FIFO only if it exists
if [ -p "$INPUT_FIFO" ]; then
    echo "" > "$INPUT_FIFO" 2>/dev/null || true
    # Wait a bit for graceful shutdown
    sleep 2
else
    # FIFO doesn't exist, skip graceful shutdown
    sleep 1
fi

# Check if process is still running
if ps -p "$SERVER_PID" > /dev/null 2>&1; then
    echo "Sending SIGTERM..."
    kill -TERM "$SERVER_PID" 2>/dev/null || true
    sleep 2
fi

# Force kill if still running
if ps -p "$SERVER_PID" > /dev/null 2>&1; then
    echo "Sending SIGKILL..."
    kill -KILL "$SERVER_PID" 2>/dev/null || true
    sleep 1
fi

# Kill any FIFO keeper processes
pkill -f "sleep.*$INPUT_FIFO" 2>/dev/null || true

# Verify process is stopped
if ps -p "$SERVER_PID" > /dev/null 2>&1; then
    echo "✗ Failed to stop server"
    exit 1
else
    echo "✓ Server stopped successfully"
    rm -f "$PID_FILE" "$INPUT_FIFO"
    echo ""
fi
