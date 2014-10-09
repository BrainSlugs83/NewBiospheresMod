@echo off
SET ERRORLEVEL=
VERIFY OTHER 2>nul
SETLOCAL ENABLEDELAYEDEXPANSION

set /A startedAt=(%time:~0,2% * 3600) + (%time:~3,2% * 60) + (%time:~6,2%)

del *.jar 2>nul 1>nul
call gradlew build
if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

set datestamp=%date:~10,4%-%date:~4,2%-%date:~7,2%
set /A timestamp=(%time:~0,2% * 3600) + (%time:~3,2% * 60) + (%time:~6,2%)
set outputFolder=.\build\libs\
set zipName="Source (%datestamp%-%timestamp%).zip"

for /F %%f in ('dir "%outputFolder%*.jar" /b') do (

    rem copy "%outputFolder%%%f" . /y
    move /y "%outputFolder%%%f" .
    if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

    zip -9 -r %zipName% src
    if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

    zip -9 -m "%%f" %zipName%
    if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%
)

set /A endedAt=(%time:~0,2% * 3600) + (%time:~3,2% * 60) + (%time:~6,2%)
set /A elapsed=(%endedAt% - %startedAt%)
if "%elapsed:~0,1%" == "-" set /a elapsed=%elapsed% + (24 * 3600)

echo.
echo SUCCESS!
echo Total Time Elapsed: %elapsed% second(s).
echo.
dir *.jar
echo.
