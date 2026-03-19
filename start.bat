@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

REM ============================================
REM 🍲 火锅店管理系统 - Docker 一键启动 (Windows)
REM ============================================

echo.
echo   ╔═══════════════════════════════════════╗
echo   ║      🍲 火锅店管理系统 Docker 启动     ║
echo   ╚═══════════════════════════════════════╝
echo.

REM ---- 检查 Docker ----
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo   [✗] Docker 未安装或未运行
    echo       请先安装并启动 Docker Desktop
    echo       下载: https://www.docker.com/products/docker-desktop/
    pause
    exit /b 1
)
echo   [✓] Docker 已就绪

cd /d "%~dp0"
set DOCKER_BUILDKIT=0

REM ---- 启动服务 ----
echo.
echo   ▶ 启动 Docker 容器...
docker compose up -d

if %errorlevel% neq 0 (
    echo   [✗] Docker 启动失败，请检查错误信息
    pause
    exit /b 1
)

REM ---- 等待后端就绪 ----
echo.
echo   ⏳ 等待后端启动...
set /a COUNT=0
set /a TIMEOUT=60

:WAIT_LOOP
curl -s -o nul -w "%%{http_code}" http://localhost/api/dish/category/list 2>nul | findstr /r "200 401 403" >nul 2>&1
if %errorlevel% equ 0 goto READY
set /a COUNT+=2
if %COUNT% geq %TIMEOUT% (
    echo   [!] 后端启动较慢，可能仍在初始化中
    echo       可运行: docker compose logs -f backend
    goto SHOW_INFO
)
timeout /t 2 /nobreak >nul
goto WAIT_LOOP

:READY
echo   [✓] 所有服务已启动

:SHOW_INFO
echo.
echo   ╔═══════════════════════════════════════════════╗
echo   ║      🍲 火锅店管理系统 - 所有服务已启动       ║
echo   ╚═══════════════════════════════════════════════╝
echo.
echo   📊 管理后台:   http://localhost/admin
echo   🌐 用户前台:   http://localhost
echo   ⚙️  后端 API:   http://localhost/api
echo.
echo   ═══ 管理端测试账号 ═══
echo   ┌──────────┬────────────┬────────┐
echo   │   角色   │   用户名   │  密码  │
echo   ├──────────┼────────────┼────────┤
echo   │ 🔴 店长  │ admin      │ 123456 │
echo   │ 🟢 收银员│ cashier1   │ 123456 │
echo   └──────────┴────────────┴────────┘
echo.
echo   ═══ 用户端会员测试账号 ═══
echo   ┌──────────┬─────────────┬────────┐
echo   │   姓名   │   手机号    │  密码  │
echo   ├──────────┼─────────────┼────────┤
echo   │ 刘晓红   │ 18600001001 │ 001001 │
echo   │ 张明     │ 18600001002 │ 001002 │
echo   │ 王丽     │ 18600001003 │ 001003 │
echo   └──────────┴─────────────┴────────┘
echo   提示: 会员密码为手机号后6位
echo.
echo   常用命令:
echo     查看日志:  docker compose logs -f
echo     停止服务:  docker compose down
echo     重建数据:  docker compose down -v ^&^& docker compose up -d
echo.
echo   💡 按 Ctrl+C 退出日志，服务将保持后台运行
echo.
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo 📺 实时日志 (Ctrl+C 退出日志，服务保持运行)
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
docker compose logs -f
