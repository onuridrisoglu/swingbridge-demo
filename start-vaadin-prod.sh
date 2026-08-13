#!/usr/bin/env bash
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "Building Swing app..."
(cd "$ROOT/swing-app" && mvn clean package -q)

echo "Copying Swing jar to vaadin-app/applibs..."
mkdir -p "$ROOT/vaadin-app/applibs"
cp "$ROOT/swing-app/target/warehouse-swing-app-3.5.0.jar" \
   "$ROOT/vaadin-app/applibs/warehouse-swing-app-3.5.0.jar"

echo "Building production Vaadin jar (runs build-frontend / Vite)..."
# build-frontend lives in the main plugin execution now, so a plain
# package produces an optimized production bundle -- no -Pproduction needed.
(cd "$ROOT/vaadin-app" && mvn clean package -q)

# Resolve the local Maven repo so we can point --patch-module / bootclasspath
# at the swing-bridge jars. SB_VERSION is read from the pom rather than
# hardcoded here: the patch/graphics jars MUST match the swing-bridge-flow
# version packaged into the fat jar, and a hardcoded copy silently drifts
# the moment the pom is upgraded.
REPO="$(cd "$ROOT/vaadin-app" && mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout)"
SB_VERSION="$(cd "$ROOT/vaadin-app" && mvn help:evaluate -Dexpression=swing-bridge.version -q -DforceStdout)"
echo "SwingBridge version from pom: $SB_VERSION"

# application.properties sets server.port=${PORT:8888}, so mirror that default.
echo "Starting Vaadin app in PRODUCTION mode on http://localhost:${PORT:-8888} ..."
cd "$ROOT/vaadin-app"

# NOTE: no JDWP debug agent here (that's for spring-boot:run/dev only).
#
# The Swing app jar is needed in TWO places, exactly mirroring dev:
#   1. On the main classpath (bundled into the fat jar via includeSystemScope=true
#      in pom.xml) so SwingAppInitializer can init PersistenceUtil/DataSeeder at boot.
#   2. As a real on-disk file in ./applibs, because the SwingBridge per-session
#      loader resolves jars via the filesystem and cannot read a jar nested inside
#      the fat jar (ZipPath.toFile() -> UnsupportedOperationException). Hence
#      -Dapplibs.dir below points at the same folder the script copied the jar into.
java \
  --patch-module java.desktop="$REPO/com/vaadin/swing-bridge-patch/$SB_VERSION/swing-bridge-patch-$SB_VERSION.jar" \
  -Xbootclasspath/a:"$REPO/com/vaadin/swing-bridge-graphics/$SB_VERSION/swing-bridge-graphics-$SB_VERSION.jar" \
  --add-reads=java.desktop=ALL-UNNAMED \
  --add-exports=java.desktop/sun.font=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt.dnd=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt.dnd.peer=ALL-UNNAMED \
  --add-exports=java.base/sun.nio.cs=ALL-UNNAMED \
  --add-exports=java.desktop/sun.java2d=ALL-UNNAMED \
  --add-exports=java.desktop/sun.java2d.pipe=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt.datatransfer=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt.image=ALL-UNNAMED \
  --add-exports=java.desktop/java.awt.peer=ALL-UNNAMED \
  --add-exports=java.desktop/java.awt.dnd=ALL-UNNAMED \
  --add-exports=java.desktop/java.awt.dnd.peer=ALL-UNNAMED \
  --add-exports=java.desktop/sun.print=ALL-UNNAMED \
  --add-exports=java.desktop/sun.swing=ALL-UNNAMED \
  --add-opens=java.desktop/java.awt.event=ALL-UNNAMED \
  --add-opens=java.desktop/sun.awt=ALL-UNNAMED \
  --add-opens=java.desktop/java.awt.dnd=ALL-UNNAMED \
  --add-opens=java.base/java.lang=ALL-UNNAMED \
  -Djava.awt.headless=false \
  -Dapplibs.dir=./applibs \
  -jar target/warehouse-vaadin-app-1.0-SNAPSHOT.jar
