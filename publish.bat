@echo off
SET ERRORLEVEL=
VERIFY OTHER 2>nul
SETLOCAL ENABLEDELAYEDEXPANSION
IF ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

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

    rem zip -9 -r %zipName% src
    rem if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

    rem zip -9 -m "%%f" %zipName%
    rem if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

    zip -9 "%%f" MODINFO.TXT
    if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

    call include.bat "%%f" "lib\concurrentlinkedhashmap-lru-1.4.jar"
    if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

    rem this improves the compression slightly:
    rd /s /q temp 2>nul 1>nul
    md temp
    copy "%%f" temp
    cd temp
    unzip "%%~nxf"
    del "%%~nxf"
    jar cvf "%%~nxf" *
    copy /y "%%~nxf" ..
    cd ..
    rd /s /q temp
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
