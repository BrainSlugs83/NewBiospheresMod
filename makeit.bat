@echo off
del *.jar 2>nul 1>nul
call gradlew build 
move build\libs\*.jar .
