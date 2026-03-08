@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title 🍲 火锅店管理系统 - 一键启动

REM ============================================
REM 火锅店管理系统 - 一键启动脚本 (Windows)
REM 使用 Chocolatey 自动安装: Java17, Maven, Node, pnpm, MySQL
REM 需要以管理员权限运行
REM ============================================

set "PROJECT_DIR=%~dp0"
set "DB_HOST=localhost"
set "DB_PORT=3306"
set "DB_NAME=hotpot_db"
set "DB_USER=root"
set "DB_PASS=ab123168"
set "BACKEND_PORT=8080"
set "ADMIN_PORT=3006"
set "FRONTEND_PORT=5173"
set "LOGS_DIR=%PROJECT_DIR%.logs"

REM 创建日志目录
if not exist "%LOGS_DIR%" mkdir "%LOGS_DIR%"

echo.
echo  ╔═══════════════════════════════════════╗
echo  ║      🍲 火锅店管理系统 启动脚本       ║
echo  ╚═══════════════════════════════════════╝
echo.

REM ============================================
REM [1/7] 检查并安装基础环境 (Chocolatey)
REM ============================================
echo [1/7] 🔍 检查基础环境...
echo.

REM 1.0 检查管理员权限
net session >nul 2>&1
if errorlevel 1 (
    echo [!] 当前非管理员权限，部分安装功能可能无法执行
    echo [!] 建议右键此脚本 -^> "以管理员身份运行"
    echo.
)

REM 1.1 检查并安装 Chocolatey
where choco >nul 2>&1
if errorlevel 1 (
    echo [!] Chocolatey 未安装，正在安装...
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))"
    REM 刷新环境变量
    call refreshenv >nul 2>&1
    set "PATH=%ALLUSERSPROFILE%\chocolatey\bin;%PATH%"
    where choco >nul 2>&1
    if errorlevel 1 (
        echo [✗] Chocolatey 安装失败，请手动安装: https://chocolatey.org/install
        pause
        exit /b 1
    )
    echo [✓] Chocolatey 安装成功
) else (
    echo [✓] Chocolatey ✓
)

REM 1.2 检查 Java 17
echo.
where java >nul 2>&1
if errorlevel 1 (
    echo [!] Java 未安装，正在通过 Chocolatey 安装 Temurin JDK 17...
    choco install temurin17 -y
    call refreshenv >nul 2>&1
    where java >nul 2>&1
    if errorlevel 1 (
        echo [✗] Java 安装失败
        pause
        exit /b 1
    )
    echo [✓] Java 17 安装成功
) else (
    for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VER=%%i"
    echo !JAVA_VER! | findstr "17\." >nul 2>&1
    if errorlevel 1 (
        echo [!] 当前 Java 版本不是 17: !JAVA_VER!
        echo [!] 正在通过 Chocolatey 安装 Temurin JDK 17...
        choco install temurin17 -y
        call refreshenv >nul 2>&1
        echo [✓] Java 17 安装成功（可能需要重启终端）
    ) else (
        echo [✓] Java ✓ ^(!JAVA_VER!^)
    )
)

REM 1.3 检查 Maven
where mvn >nul 2>&1
if errorlevel 1 (
    echo [!] Maven 未安装，正在通过 Chocolatey 安装...
    choco install maven -y
    call refreshenv >nul 2>&1
    where mvn >nul 2>&1
    if errorlevel 1 (
        echo [✗] Maven 安装失败
        pause
        exit /b 1
    )
    echo [✓] Maven 安装成功
) else (
    echo [✓] Maven ✓
)

REM 1.4 检查 Node.js
where node >nul 2>&1
if errorlevel 1 (
    echo [!] Node.js 未安装，正在通过 Chocolatey 安装...
    choco install nodejs-lts -y
    call refreshenv >nul 2>&1
    where node >nul 2>&1
    if errorlevel 1 (
        echo [✗] Node.js 安装失败
        pause
        exit /b 1
    )
    echo [✓] Node.js 安装成功
) else (
    for /f "tokens=*" %%i in ('node -v 2^>nul') do set NODE_VER=%%i
    echo [✓] Node.js ✓ ^(!NODE_VER!^)
)

REM 1.5 检查 pnpm
where pnpm >nul 2>&1
if errorlevel 1 (
    echo [!] pnpm 未安装，正在通过 Chocolatey 安装...
    choco install pnpm -y
    call refreshenv >nul 2>&1
    where pnpm >nul 2>&1
    if errorlevel 1 (
        echo [!] choco 安装 pnpm 失败，尝试 npm 安装...
        call npm install -g pnpm
    )
    where pnpm >nul 2>&1
    if errorlevel 1 (
        echo [✗] pnpm 安装失败
        pause
        exit /b 1
    )
    echo [✓] pnpm 安装成功
) else (
    for /f "tokens=*" %%i in ('pnpm -v 2^>nul') do set PNPM_VER=%%i
    echo [✓] pnpm ✓ ^(!PNPM_VER!^)
)

REM 1.6 检查 MySQL
where mysql >nul 2>&1
if errorlevel 1 (
    echo [!] MySQL 未安装，正在通过 Chocolatey 安装...
    choco install mysql -y
    call refreshenv >nul 2>&1
    where mysql >nul 2>&1
    if errorlevel 1 (
        echo [✗] MySQL 安装失败
        echo [!] 请手动安装 MySQL 并确保 mysql 命令可用
        pause
        exit /b 1
    )
    echo [✓] MySQL 安装成功
) else (
    echo [✓] MySQL ✓
)

REM ============================================
REM [2/7] 检查 MySQL 数据库
REM ============================================
echo.
echo [2/7] 🗄️ 检查 MySQL 数据库...

REM 检查 MySQL 服务是否运行
mysqladmin ping -h%DB_HOST% -u%DB_USER% -p%DB_PASS% --silent >nul 2>&1
if errorlevel 1 (
    echo [!] MySQL 服务未运行，尝试启动...
    net start mysql >nul 2>&1
    timeout /t 3 /nobreak >nul
    mysqladmin ping -h%DB_HOST% -u%DB_USER% -p%DB_PASS% --silent >nul 2>&1
    if errorlevel 1 (
        echo [✗] MySQL 无法启动，请手动启动 MySQL 服务
        pause
        exit /b 1
    )
)
echo [✓] MySQL 服务运行中

REM 检查数据库是否存在
mysql -h%DB_HOST% -u%DB_USER% -p%DB_PASS% -e "USE %DB_NAME%;" >nul 2>&1
if errorlevel 1 (
    echo [!] 数据库 %DB_NAME% 不存在，正在创建并导入数据...
    mysql -h%DB_HOST% -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 < "%PROJECT_DIR%sql\init.sql"
    if errorlevel 1 (
        echo [✗] init.sql 导入失败
        pause
        exit /b 1
    )
    mysql -h%DB_HOST% -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 < "%PROJECT_DIR%sql\data.sql"
    if errorlevel 1 (
        echo [✗] data.sql 导入失败
        pause
        exit /b 1
    )
    echo [✓] 数据库初始化完成
) else (
    echo [✓] 数据库 %DB_NAME% 已存在
)

REM ============================================
REM [3/7] 安装项目依赖
REM ============================================
echo.
echo [3/7] 📦 安装项目依赖...

REM 后端编译
if not exist "%PROJECT_DIR%backend\target\classes" (
    echo [!] 编译后端项目...
    cd /d "%PROJECT_DIR%backend" && call mvn compile -q
    if errorlevel 1 (
        echo [✗] 后端编译失败
        pause
        exit /b 1
    )
)
echo [✓] 后端编译完成

REM 管理端依赖
if not exist "%PROJECT_DIR%admin\node_modules" (
    echo [!] 安装管理端依赖...
    cd /d "%PROJECT_DIR%admin" && call pnpm install
    if errorlevel 1 (
        echo [✗] 管理端依赖安装失败
        pause
        exit /b 1
    )
)
echo [✓] 管理端依赖就绪

REM 用户端依赖
if not exist "%PROJECT_DIR%frontend\node_modules" (
    echo [!] 安装用户端依赖...
    cd /d "%PROJECT_DIR%frontend" && call pnpm install
    if errorlevel 1 (
        echo [✗] 用户端依赖安装失败
        pause
        exit /b 1
    )
)
echo [✓] 用户端依赖就绪

REM ============================================
REM [4/7] 检查并清理端口
REM ============================================
echo.
echo [4/7] 🔌 检查端口冲突...

REM 检查并 kill 端口占用进程
call :kill_port %BACKEND_PORT% "后端 Spring Boot"
call :kill_port %ADMIN_PORT% "管理端 Vite"
call :kill_port %FRONTEND_PORT% "用户端 Vite"
echo [✓] 所有端口可用

REM ============================================
REM [5/7] 启动所有服务
REM ============================================
echo.
echo [5/7] 🚀 启动所有服务...

REM 启动后端（独立窗口，显示报错）
echo [✓] 启动后端 (Spring Boot @ :%BACKEND_PORT%)...
start "🔴 火锅店后端服务" cmd /k "cd /d %PROJECT_DIR%backend && color 0A && echo ═══ 火锅店后端 Spring Boot ═══ && echo. && mvn spring-boot:run 2>&1 | more"

timeout /t 2 /nobreak >nul

REM 启动管理端（独立窗口，显示报错）
echo [✓] 启动管理端 (Vite @ :%ADMIN_PORT%)...
start "🟢 火锅店管理端" cmd /k "cd /d %PROJECT_DIR%admin && color 0B && echo ═══ 火锅店管理端 Vite ═══ && echo. && pnpm dev 2>&1"

timeout /t 2 /nobreak >nul

REM 启动用户端（独立窗口，显示报错）
echo [✓] 启动用户端 (Vite @ :%FRONTEND_PORT%)...
start "🔵 火锅店用户端" cmd /k "cd /d %PROJECT_DIR%frontend && color 0E && echo ═══ 火锅店用户端 Vite ═══ && echo. && pnpm dev 2>&1"

REM ============================================
REM [6/7] 等待服务就绪
REM ============================================
echo.
echo [6/7] ⏳ 等待服务就绪 (约30-120秒)...

:wait_backend
timeout /t 3 /nobreak >nul
powershell -command "try { (New-Object Net.Sockets.TcpClient).Connect('localhost', %BACKEND_PORT%); exit 0 } catch { exit 1 }" >nul 2>&1
if errorlevel 1 goto wait_backend
echo [✓] 后端已就绪 (端口 %BACKEND_PORT%)

:wait_admin
timeout /t 2 /nobreak >nul
powershell -command "try { (New-Object Net.Sockets.TcpClient).Connect('localhost', %ADMIN_PORT%); exit 0 } catch { exit 1 }" >nul 2>&1
if errorlevel 1 goto wait_admin
echo [✓] 管理端已就绪 (端口 %ADMIN_PORT%)

:wait_frontend
timeout /t 2 /nobreak >nul
powershell -command "try { (New-Object Net.Sockets.TcpClient).Connect('localhost', %FRONTEND_PORT%); exit 0 } catch { exit 1 }" >nul 2>&1
if errorlevel 1 goto wait_frontend
echo [✓] 用户端已就绪 (端口 %FRONTEND_PORT%)

REM ============================================
REM [7/7] 启动完成
REM ============================================
echo.
echo ═══════════════════════════════════════════════
echo   🍲 火锅店管理系统 - 所有服务已启动
echo ═══════════════════════════════════════════════
echo.
echo   📊 管理端:   http://localhost:%ADMIN_PORT%
echo   🌐 用户端:   http://localhost:%FRONTEND_PORT%
echo   ⚙️  后端API:  http://localhost:%BACKEND_PORT%
echo.
echo   ═══ 测试账号 ═══
echo   ┌──────────┬────────────┬────────┐
echo   │   角色   │   用户名   │  密码  │
echo   ├──────────┼────────────┼────────┤
echo   │ 🔴 店长  │ admin      │ 123456 │
echo   │ 🟢 收银员│ cashier1   │ 123456 │
echo   │ 🟡 后厨  │ kitchen1   │ 123456 │
echo   │ 🔵 库管员│ inventory1 │ 123456 │
echo   └──────────┴────────────┴────────┘
echo   会员登录: 手机号 + 手机号后6位作为密码
echo.
echo   💡 关闭各服务窗口或运行 stop.bat 停止服务
echo   ⚠️  各服务的报错信息会在对应窗口中显示
echo.
pause
goto :eof

REM ============================================
REM 函数: 终止占用端口的进程
REM ============================================
:kill_port
set "PORT=%~1"
set "NAME=%~2"
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
    echo [!] 端口 %PORT% (%NAME%) 被占用 (PID: %%a)，正在强制释放...
    taskkill /PID %%a /F >nul 2>&1
    timeout /t 1 /nobreak >nul
)
goto :eof
