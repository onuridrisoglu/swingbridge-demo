@echo off
setlocal

cd /d "%~dp0swing-app"
mvn clean package -q
java -jar target\warehouse-swing-app-3.5.0.jar
