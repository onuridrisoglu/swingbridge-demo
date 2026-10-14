#!/usr/bin/env bash
set -e

# Builds the NetBeans RCP app, copies its distribution into the Vaadin host, and
# starts the host that embeds the RCP via the Vaadin Swing Bridge (NetBeansRcpBridge).
#
# Requires JDK 21 (NetBeans Platform 25 + -Djava.security.manager=allow).
#
# Usage: ./start-vaadin.sh [full|views]
#   full  — the entire NetBeans RCP shell at /
#   views — Vaadin shell with one embedded TopComponent per route
# Without an argument, warehouse.rcp.mode from vaadin-app's application.properties applies.

ROOT="$(cd "$(dirname "$0")" && pwd)"

MODE="${1:-}"
case "$MODE" in
    ""|full|views) ;;
    *) echo "Unknown mode '$MODE' (expected: full | views)" >&2; exit 1 ;;
esac

# Build / run with JDK 21 when discoverable. Skip JetBrains Runtime: its patched
# java.desktop calls PlatformGraphicsInfo.getSplashScreenLib(), which the SwingBridge
# java.desktop patch lacks, so the JVM exits when the embedded platform boots.
if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    J21="$(/usr/libexec/java_home -V 2>&1 | grep -E '^ +21\.' | grep -v JetBrains | head -1 | sed -E 's/.* (\/.*)$/\1/')"
    if [ -z "$J21" ]; then
        J21="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
    fi
    if [ -n "$J21" ]; then
        export JAVA_HOME="$J21"
    fi
fi
echo "Using JAVA_HOME=${JAVA_HOME:-<default on PATH>}"

# `install` also puts the module's plain "domain" jar into ~/.m2 for the host.
echo "Building NetBeans RCP application + distribution..."
(cd "$ROOT/netbeans-app" && mvn clean install -q)

echo "Copying NetBeans RCP distribution to vaadin-app/applibs/warehousepro/..."
rm -rf "$ROOT/vaadin-app/applibs/warehousepro"
mkdir -p "$ROOT/vaadin-app/applibs"
cp -R "$ROOT/netbeans-app/application/target/warehousepro" \
      "$ROOT/vaadin-app/applibs/warehousepro"

echo "Freeing debug port 5006..."
lsof -ti:5006 | xargs kill -9 2>/dev/null || true

cd "$ROOT/vaadin-app"
if [ -n "$MODE" ]; then
    echo "Starting Vaadin host (embeds the NetBeans RCP, mode=$MODE)..."
    mvn spring-boot:run -Dspring-boot.run.arguments=--warehouse.rcp.mode="$MODE"
else
    echo "Starting Vaadin host (embeds the NetBeans RCP, mode from application.properties)..."
    mvn spring-boot:run
fi
