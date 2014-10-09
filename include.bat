@echo off
SET ERRORLEVEL=
VERIFY OTHER 2>nul
SETLOCAL ENABLEDELAYEDEXPANSION ENABLEEXTENSIONS
IF ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

REM FOR DEBUGGING ONLY
REM if "%1" == "" include NewBiospheresMod-0.7.jar lib\concurrentlinkedhashmap-lru-1.4.jar
REM if "%2" == "" include "%~1" lib\concurrentlinkedhashmap-lru-1.4.jar

cd /d "%~dp2"
rd /s /q temp 2>nul 1>nul

md temp
copy "%~nx2" temp
cd temp
unzip -o "%~nx2"
rd /s /q META-INF
del "%~nx2"

for /R %%f in (*.*) do (

    set file=%%f
    set file=!file:%cd%\=!

    if NOT "%%~nxf" == "dirFile.txt" (

        echo !file! >> dirFile.txt
    )
)

zip -9 -m -@ "..\..\%~nx1" < dirFile.txt
cd ..
rd /s /q temp