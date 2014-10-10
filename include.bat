@echo off
SET ERRORLEVEL=
VERIFY OTHER 2>nul
SETLOCAL ENABLEDELAYEDEXPANSION ENABLEEXTENSIONS
IF ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

REM FOR DEBUGGING ONLY (DO NOT UNCOMMENT)
REM if "%1" == "" include NewBiospheresMod-0.7.jar lib\concurrentlinkedhashmap-lru-1.4.jar
REM if "%2" == "" include "%~1" lib\concurrentlinkedhashmap-lru-1.4.jar

cd /d "%~dp2"
if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

rd /s /q temp 2>nul 1>nul

md temp
if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

copy "%~nx2" temp
if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

cd temp
if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

unzip -o "%~nx2"
if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

rd /s /q META-INF
del "%~nx2"
if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

for /R %%f in (*.*) do (

    set file=%%f
    set file=!file:%cd%\=!

    if NOT "%%~nxf" == "dirFile.txt" (

        echo !file! >> dirFile.txt
    )
)

zip -9 -m -@ "..\..\%~nx1" < dirFile.txt
if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

cd ..
rd /s /q temp
if ERRORLEVEL 1 EXIT /B %ERRORLEVEL%
