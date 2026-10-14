@echo off
setlocal

rem Builds the NetBeans RCP app, copies its distribution into the Vaadin host, and
rem starts the host that embeds the RCP via the Vaadin Swing Bridge (NetBeansRcpBridge).
rem Requires JDK 21 (set JAVA_HOME accordingly).
rem
rem Usage: start-vaadin.bat [full^|views]
rem Without an argument, warehouse.rcp.mode from vaadin-app's application.properties applies.

set ROOT=%~dp0
if "%ROOT:~-1%"=="\" set ROOT=%ROOT:~0,-1%

set MODE=%1
if not "%MODE%"=="" if /i not "%MODE%"=="full" if /i not "%MODE%"=="views" (
    echo Unknown mode '%MODE%' ^(expected: full ^| views^)
    exit /b 1
)

rem `install` also puts the module's plain "domain" jar into ~/.m2 for the host.
echo Building NetBeans RCP application + distribution...
pushd "%ROOT%\netbeans-app"
call mvn clean install -q || exit /b 1
popd

echo Copying NetBeans RCP distribution to vaadin-app\applibs\warehousepro...
if exist "%ROOT%\vaadin-app\applibs\warehousepro" rmdir /s /q "%ROOT%\vaadin-app\applibs\warehousepro"
xcopy /e /i /q /y "%ROOT%\netbeans-app\application\target\warehousepro" ^
      "%ROOT%\vaadin-app\applibs\warehousepro" >nul

echo Freeing debug port 5006...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":5006 " 2^>nul') do (
    taskkill /pid %%p /f >nul 2>&1
)

cd /d "%ROOT%\vaadin-app"
if "%MODE%"=="" (
    echo Starting Vaadin host ^(embeds the NetBeans RCP, mode from application.properties^)...
    mvn spring-boot:run
) else (
    echo Starting Vaadin host ^(embeds the NetBeans RCP, mode=%MODE%^)...
    mvn spring-boot:run -Dspring-boot.run.arguments=--warehouse.rcp.mode=%MODE%
)
