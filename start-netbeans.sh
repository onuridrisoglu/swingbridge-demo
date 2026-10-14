#!/usr/bin/env bash
set -e

# Builds and launches the NetBeans Platform (RCP) edition of WarehousePro.
# Launches on JDK 21, the same JDK the SwingBridge host (start-vaadin.sh)
# needs. The NetBeans Platform installs a Security Manager, which JDK 24+ removed;
# on 21 it's enabled via -Djava.security.manager=allow in warehousepro.conf.

ROOT="$(cd "$(dirname "$0")" && pwd)"

# Use JDK 21 if one is discoverable; otherwise fall back to the current JAVA_HOME / PATH java.
if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    if J21="$(/usr/libexec/java_home -v 21 2>/dev/null)"; then
        export JAVA_HOME="$J21"
    fi
fi
echo "Using JAVA_HOME=${JAVA_HOME:-<default on PATH>}"

cd "$ROOT/netbeans-app"

echo "Building NetBeans RCP application..."
mvn clean install -q

echo "Launching WarehousePro (NetBeans Platform)..."
cd "$ROOT/netbeans-app/application"
mvn nbm:run-platform
