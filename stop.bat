@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title 🍲 火锅店管理系统 - 停止服务

echo.
echo  ╔═══════════════════════════════════════╗
echo  ║      🍲 火锅店管理系统 停止脚本       ║
echo  ╚═══════════════════════════════════════╝
echo.

set "BACKEND_PORT=8080"
set "ADMIN_PORT=3006"
set "FRONTEND_PORT=5173"

REM 通过端口查找并终止进程
call :stop_port %BACKEND_PORT% "后端 Spring Boot"
call :stop_port %ADMIN_PORT% "管理端 Vite"
call :stop_port %FRONTEND_PORT% "用户端 Vite"

REM 关闭相关的 cmd 窗口
taskkill /FI "WINDOWTITLE eq 🔴 火锅店后端服务*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq 🟢 火锅店管理端*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq 🔵 火锅店用户端*" /F >nul 2>&1

echo.
echo [✓] 所有服务已停止
echo.
pause
goto :eof

:stop_port
set "PORT=%~1"
set "NAME=%~2"
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
    echo [✓] 正在停止 %NAME% (端口 %PORT%, PID: %%a)...
    taskkill /PID %%a /F >nul 2>&1
)
goto :eof
