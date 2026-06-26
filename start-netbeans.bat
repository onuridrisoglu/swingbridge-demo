@echo off
setlocal

set ROOT=%~dp0
if "%ROOT:~-1%"=="\" set ROOT=%ROOT:~0,-1%

echo Building NetBeans RCP application...
pushd "%ROOT%\netbeans-app"
mvn clean install -q
popd

echo Launching WarehousePro (NetBeans Platform)...
cd /d "%ROOT%\netbeans-app\application"
mvn nbm:run-platform
