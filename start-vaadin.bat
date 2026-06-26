@echo off
setlocal

set ROOT=%~dp0
if "%ROOT:~-1%"=="\" set ROOT=%ROOT:~0,-1%

echo Building Swing app...
pushd "%ROOT%\swing-app"
mvn clean package -q
popd

echo Copying Swing jar to vaadin-app\applibs...
if not exist "%ROOT%\vaadin-app\applibs" mkdir "%ROOT%\vaadin-app\applibs"
copy /y "%ROOT%\swing-app\target\warehouse-swing-app-3.5.0.jar" ^
        "%ROOT%\vaadin-app\applibs\warehouse-swing-app-3.5.0.jar"

echo Freeing debug port 5005...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":5005 " 2^>nul') do (
    taskkill /pid %%p /f >nul 2>&1
)

echo Starting Vaadin app...
cd /d "%ROOT%\vaadin-app"
mvn spring-boot:run
