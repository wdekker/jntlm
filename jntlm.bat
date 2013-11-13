@echo off
setlocal ENABLEDELAYEDEXPANSION

REM Starts the proxy proxy
REM Find out information about this program
SET APP_HOME=%~dp0
SET TARGET=%APP_HOME%\target
SET LIBS=%TARGET%\dependency
SET CLASSPATH=%TARGET%\classes

CALL mvn compile
CALL mvn dependency:copy-dependencies

REM Build the classpath.
for /F %%a in ('dir /B /S %LIBS%') DO (SET CLASSPATH=!CLASSPATH!;%%a)

echo %CLASSPATH%
call java -cp %CLASSPATH% nl.willem.http.jntlm.JNTLM %*