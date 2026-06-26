#!/usr/bin/env bash
set -e

# Builds the NetBeans RCP app, copies its distribution into the Vaadin host, and
# starts the host that embeds the RCP via the Vaadin Swing Bridge (NetBeansRcpBridge).
#
# Requires JDK 21 (NetBeans Platform 25 + -Djava.security.manager=allow).

ROOT="$(cd "$(dirname "$0")" && pwd)"

# Build / run with JDK 21 when discoverable.
if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    if J21="$(/usr/libexec/java_home -v 21 2>/dev/null)"; then
        export JAVA_HOME="$J21"
    fi
fi
echo "Using JAVA_HOME=${JAVA_HOME:-<default on PATH>}"

echo "Building NetBeans RCP application + distribution..."
(cd "$ROOT/netbeans-app" && mvn clean install -q)

echo "Copying domain jar to vaadin-app-nb/lib/..."
mkdir -p "$ROOT/vaadin-app-nb/lib"
cp "$ROOT/netbeans-app/warehouse/target/warehouse-3.5.0.jar" \
   "$ROOT/vaadin-app-nb/lib/warehouse-3.5.0.jar"

echo "Copying NetBeans RCP distribution to vaadin-app-nb/applibs/warehousepro/..."
rm -rf "$ROOT/vaadin-app-nb/applibs/warehousepro"
mkdir -p "$ROOT/vaadin-app-nb/applibs"
cp -R "$ROOT/netbeans-app/application/target/warehousepro" \
      "$ROOT/vaadin-app-nb/applibs/warehousepro"

echo "Freeing debug port 5006..."
lsof -ti:5006 | xargs kill -9 2>/dev/null || true

echo "Starting Vaadin host (embeds the NetBeans RCP)..."
cd "$ROOT/vaadin-app-nb"
mvn spring-boot:run
