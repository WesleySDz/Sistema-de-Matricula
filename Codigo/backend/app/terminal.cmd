@echo off
setlocal

pushd "%~dp0" || exit /b 1
call mvnw.cmd -q -DskipTests package
if errorlevel 1 goto fim

java -jar target\matricula-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=terminal %*

:fim
set "terminal_exit=%errorlevel%"
popd
endlocal & exit /b %terminal_exit%
