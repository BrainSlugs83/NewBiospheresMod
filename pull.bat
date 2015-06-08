@echo off
SETLOCAL

echo ARE YOU SURE YOU WANT TO GET THE LATEST CODEBASE?
echo (ALL PENDING CHANGES IN THIS BRANCH WILL BE LOST!)
set /p ans=^>
if /i NOT "%ans:~0,1%" == "y" EXIT /B 1

:: bzr pull https://github.com/BrainSlugs83/NewBiospheresMod.git/,branch=master 

echo.
echo ===== Pulling =====
git pull

echo.
echo ===== Resetting to Latest =====
git reset --hard HEAD

