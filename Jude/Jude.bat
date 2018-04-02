@echo off
rem Batch file to run Jude on Windows

rem set JAVA_HOME=C:\j2sdk1.4.1_02
rem set PATH=%JAVA_HOME%\bin

set JUDE_JAR=jude-take.jar

rem JUDE_HOME should be this folder
if not "%JUDE_HOME%"=="" goto hasJudeHome

if "%OS%"=="Windows_NT" goto setJudeHomeNT

rem *** Windows98 user have to edit here ****
rem set JUDE_HOME=c:\Program Files\Jude

if "%JUDE_HOME%"=="" goto noJudeHome
if not exist "%JUDE_HOME%\%JUDE_JAR%" goto badJudeHome
goto hasJudeHome

rem for Windows2000, XP, NT
:setJudeHomeNT
set JUDE_HOME=%~dp0

:hasJudeHome

rem set CLASSPATH=%JUDE_HOME%\%JUDE_JAR%

rem run Jude
start javaw -Xms16m -Xmx256m -Xss1m -jar "%JUDE_HOME%\%JUDE_JAR%"  %1 %2 %3
rem start javaw -jar "%JUDE_HOME%\%JUDE_JAR%"  %1 %2 %3
goto end

:noJudeHome
echo.
echo JUDE_HOME is not set.  Please set JUDE_HOME environment variable
echo or edit jude-take.bat to set JUDE_HOME.
echo. 
pause
goto end

:badJudeHome
echo.
echo JUDE_HOME is invalid.  Please check your JUDE_HOME.
echo. 
pause
goto end

:end
