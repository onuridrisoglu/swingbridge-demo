#!/usr/bin/env bash
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "Building Swing app..."
(cd "$ROOT/swing-app" && mvn clean package -q)

echo "Copying Swing jar to vaadin-app/applibs..."
mkdir -p "$ROOT/vaadin-app/applibs"
cp "$ROOT/swing-app/target/warehouse-swing-app-3.5.0.jar" \
   "$ROOT/vaadin-app/applibs/warehouse-swing-app-3.5.0.jar"

echo "Freeing debug port 5005..."
lsof -ti:5005 | xargs kill -9 2>/dev/null || true

echo "Starting Vaadin app..."
cd "$ROOT/vaadin-app"
mvn spring-boot:run
