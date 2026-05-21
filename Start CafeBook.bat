@echo off
title CafeBook — Starting...
cd /d "%~dp0backend"
echo.
echo  ==========================================
echo   CafeBook — Cafe Seat Booking System
echo  ==========================================
echo.
echo  Starting server, please wait...
echo  App will open at: http://localhost:8080
echo.
start /b "" cmd /c "timeout /t 18 /nobreak >nul && start http://localhost:8080"
mvn spring-boot:run
pause
