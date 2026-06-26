#!/usr/bin/env bash
set -e

# Builds and launches the NetBeans Platform (RCP) edition of WarehousePro.
# The NetBeans Platform launches on JDK 17 here for maximum compatibility;
# override by exporting JAVA_HOME before running.

ROOT="$(cd "$(dirname "$0")" && pwd)"

# Prefer a JDK 17 if one is discoverable; otherwise use the current JAVA_HOME / PATH java.
if [ -z "${JAVA_HOME:-}" ] && command -v /usr/libexec/java_home >/dev/null 2>&1; then
    if J17="$(/usr/libexec/java_home -v 17 2>/dev/null)"; then
        export JAVA_HOME="$J17"
    fi
fi
echo "Using JAVA_HOME=${JAVA_HOME:-<default on PATH>}"

cd "$ROOT/netbeans-app"

echo "Building NetBeans RCP application..."
mvn clean install -q

echo "Launching WarehousePro (NetBeans Platform)..."
cd "$ROOT/netbeans-app/application"
mvn nbm:run-platform
