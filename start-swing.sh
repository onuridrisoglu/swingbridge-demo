#!/usr/bin/env bash
set -e

cd "$(dirname "$0")/swing-app"
mvn clean package -q
java -jar target/warehouse-swing-app-3.5.0.jar
