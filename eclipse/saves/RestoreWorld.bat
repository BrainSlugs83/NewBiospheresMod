@echo off
setlocal
rd /s /q "Copy of New World"
md "Copy of New World"
copy "Copy of New World.zip" ".\Copy of New World"
cd "Copy of New World"
unzip "Copy of New World.zip"
del "Copy of New World.zip"