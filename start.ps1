# ============================================
# 🍲 火锅店管理系统 - 一键启动脚本 (Windows PowerShell)
# 自动检测并安装: Chocolatey, Java17, Maven, Node, pnpm, MySQL
# ============================================
# 使用方法: 右键 start.ps1 → "使用 PowerShell 运行"
#           或双击 start.bat
# ============================================

$ErrorActionPreference = "Continue"
$Host.UI.RawUI.WindowTitle = "🍲 火锅店管理系统 - 一键启动"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# 项目配置
$PROJECT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Definition
$DB_HOST = "localhost"
$DB_PORT = "3306"
$DB_NAME = "hotpot_db"
$DB_USER = "root"
$DB_PASS = "ab123168"
$BACKEND_PORT = 8080
$ADMIN_PORT = 3006
$FRONTEND_PORT = 5173
$LOG_DIR = Join-Path $PROJECT_DIR ".logs"

# PID 跟踪
$script:StartedProcesses = @()

# ============================================
# 工具函数
# ============================================
function Write-Banner {
    Write-Host ""
    Write-Host "  ╔═══════════════════════════════════════╗" -ForegroundColor Red
    Write-Host "  ║      🍲 火锅店管理系统 启动脚本       ║" -ForegroundColor Red
    Write-Host "  ╚═══════════════════════════════════════╝" -ForegroundColor Red
    Write-Host ""
}

function Write-Info  { param($msg) Write-Host "[✓] $msg" -ForegroundColor Green }
function Write-Warn  { param($msg) Write-Host "[!] $msg" -ForegroundColor Yellow }
function Write-Err   { param($msg) Write-Host "[✗] $msg" -ForegroundColor Red }
function Write-Step  { param($msg) Write-Host "`n$msg" -ForegroundColor Cyan }

function Test-Command { param($cmd) return [bool](Get-Command $cmd -ErrorAction SilentlyContinue) }

function Stop-PortProcess {
    param($Port, $Name)
    $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        Write-Warn "端口 $Port ($Name) 被占用，正在强制释放..."
        $conn | ForEach-Object {
            Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue
        }
        Start-Sleep -Seconds 1
        $still = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
        if ($still) {
            Write-Err "无法释放端口 $Port，请手动处理"
            exit 1
        }
        Write-Info "端口 $Port 已释放"
    }
}

function Wait-Port {
    param($Port, $Name, $Timeout = 120)
    $count = 0
    while ($count -lt $Timeout) {
        try {
            $tcp = New-Object System.Net.Sockets.TcpClient
            $tcp.Connect("localhost", $Port)
            $tcp.Close()
            Write-Info "$Name 已就绪 ✓ (端口 $Port, 耗时 ${count}s)"
            return $true
        } catch {
            $count++
            Write-Host "`r  ⏳ 等待 $Name 就绪... (${count}s)" -NoNewline
            Start-Sleep -Seconds 1
        }
    }
    Write-Host ""
    Write-Err "$Name 启动超时（${Timeout}秒），请检查日志"
    return $false
}

# ============================================
# 主流程
# ============================================
Write-Banner

# ------------------------------------------
# [1/7] 检查并安装基础环境
# ------------------------------------------
Write-Step "[1/7] 🔍 检查基础环境..."

# 1.1 检查 Chocolatey
if (-not (Test-Command "choco")) {
    Write-Warn "Chocolatey 未安装，正在安装..."
    Set-ExecutionPolicy Bypass -Scope Process -Force
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    try {
        Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
        # 刷新环境变量
        $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
    } catch {
        Write-Err "Chocolatey 安装失败: $_"
        exit 1
    }
    if (-not (Test-Command "choco")) {
        Write-Err "Chocolatey 安装失败，请手动安装: https://chocolatey.org/install"
        exit 1
    }
    Write-Info "Chocolatey 安装成功"
} else {
    Write-Info "Chocolatey ✓"
}

# 辅助: 用 choco 安装后刷新 PATH
function Install-WithChoco {
    param($Package, $Name)
    Write-Warn "$Name 未安装，正在通过 Chocolatey 安装..."
    choco install $Package -y --no-progress | Out-Null
    # 刷新 PATH
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
}

# 1.2 检查 Java 17
if (-not (Test-Command "java")) {
    Install-WithChoco "temurin17" "Java 17"
    if (-not (Test-Command "java")) {
        Write-Err "Java 安装失败"
        exit 1
    }
    Write-Info "Java 17 安装成功"
} else {
    $javaVer = (java -version 2>&1 | Select-Object -First 1)
    Write-Info "Java ✓ ($javaVer)"
}

# 1.3 检查 Maven
if (-not (Test-Command "mvn")) {
    Install-WithChoco "maven" "Maven"
    if (-not (Test-Command "mvn")) {
        Write-Err "Maven 安装失败"
        exit 1
    }
    Write-Info "Maven 安装成功"
} else {
    Write-Info "Maven ✓"
}

# 1.4 检查 Node.js
if (-not (Test-Command "node")) {
    Install-WithChoco "nodejs-lts" "Node.js"
    if (-not (Test-Command "node")) {
        Write-Err "Node.js 安装失败"
        exit 1
    }
    Write-Info "Node.js 安装成功"
} else {
    Write-Info "Node.js ✓ ($(node -v))"
}

# 1.5 检查 pnpm
if (-not (Test-Command "pnpm")) {
    Install-WithChoco "pnpm" "pnpm"
    if (-not (Test-Command "pnpm")) {
        # 回退: 用 npm 安装
        npm install -g pnpm 2>$null
    }
    if (-not (Test-Command "pnpm")) {
        Write-Err "pnpm 安装失败"
        exit 1
    }
    Write-Info "pnpm 安装成功"
} else {
    Write-Info "pnpm ✓ ($(pnpm -v))"
}

# 1.6 检查 MySQL
if (-not (Test-Command "mysql")) {
    Install-WithChoco "mysql" "MySQL"
    if (-not (Test-Command "mysql")) {
        Write-Err "MySQL 安装失败"
        exit 1
    }
    Write-Info "MySQL 安装成功"
} else {
    Write-Info "MySQL ✓"
}

# ------------------------------------------
# [2/7] 检查 MySQL 数据库
# ------------------------------------------
Write-Step "[2/7] 🗄️  检查 MySQL 数据库..."

# 检查 MySQL 服务是否运行
try {
    $mysqlPing = mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1" 2>&1
    if ($LASTEXITCODE -ne 0) { throw "MySQL not running" }
    Write-Info "MySQL 服务运行中"
} catch {
    Write-Warn "MySQL 未运行，尝试启动..."
    net start mysql 2>$null
    Start-Sleep -Seconds 3
    try {
        mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1" 2>&1 | Out-Null
        Write-Info "MySQL 服务已启动"
    } catch {
        Write-Err "MySQL 无法启动，请手动检查"
        exit 1
    }
}

# 检查数据库是否存在
$dbCheck = mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" --default-character-set=utf8mb4 -N -e "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='$DB_NAME'" 2>$null
if (-not $dbCheck -or $dbCheck.Trim() -eq "0") {
    Write-Info "数据库 $DB_NAME 不存在，正在创建并导入数据..."
    $initSql = Join-Path $PROJECT_DIR "sql\init.sql"
    $dataSql = Join-Path $PROJECT_DIR "sql\data.sql"
    mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" --default-character-set=utf8mb4 -e "source $initSql"
    if ($LASTEXITCODE -ne 0) { Write-Err "init.sql 导入失败"; exit 1 }
    mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" --default-character-set=utf8mb4 -e "source $dataSql"
    if ($LASTEXITCODE -ne 0) { Write-Err "data.sql 导入失败"; exit 1 }
    Write-Info "数据库初始化完成 ✓"
} else {
    $tableCount = mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" --default-character-set=utf8mb4 -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME'" 2>$null
    if (-not $tableCount -or $tableCount.Trim() -eq "0") {
        Write-Info "数据库为空，正在导入..."
        $initSql = Join-Path $PROJECT_DIR "sql\init.sql"
        $dataSql = Join-Path $PROJECT_DIR "sql\data.sql"
        mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" --default-character-set=utf8mb4 -e "source $initSql"
        mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" --default-character-set=utf8mb4 -e "source $dataSql"
        Write-Info "数据库初始化完成 ✓"
    } else {
        Write-Info "数据库 $DB_NAME 已存在 ($($tableCount.Trim()) 张表)"
    }
}

# ------------------------------------------
# [3/7] 安装项目依赖
# ------------------------------------------
Write-Step "[3/7] 📦 安装项目依赖..."

# 后端编译
$backendClasses = Join-Path $PROJECT_DIR "backend\target\classes"
if (-not (Test-Path $backendClasses)) {
    Write-Info "编译后端项目..."
    Push-Location (Join-Path $PROJECT_DIR "backend")
    mvn compile -q
    if ($LASTEXITCODE -ne 0) { Write-Err "后端编译失败"; Pop-Location; exit 1 }
    Pop-Location
    Write-Info "后端编译完成 ✓"
} else {
    Write-Info "后端已编译 ✓"
}

# 管理端依赖
$adminModules = Join-Path $PROJECT_DIR "admin\node_modules"
if (-not (Test-Path $adminModules)) {
    Write-Info "安装管理端依赖..."
    Push-Location (Join-Path $PROJECT_DIR "admin")
    pnpm install
    if ($LASTEXITCODE -ne 0) { Write-Err "管理端依赖安装失败"; Pop-Location; exit 1 }
    Pop-Location
}
Write-Info "管理端依赖就绪 ✓"

# 用户端依赖
$frontendModules = Join-Path $PROJECT_DIR "frontend\node_modules"
if (-not (Test-Path $frontendModules)) {
    Write-Info "安装用户端依赖..."
    Push-Location (Join-Path $PROJECT_DIR "frontend")
    pnpm install
    if ($LASTEXITCODE -ne 0) { Write-Err "用户端依赖安装失败"; Pop-Location; exit 1 }
    Pop-Location
}
Write-Info "用户端依赖就绪 ✓"

# ------------------------------------------
# [4/7] 检查并清理端口
# ------------------------------------------
Write-Step "[4/7] 🔌 检查端口冲突..."

Stop-PortProcess -Port $BACKEND_PORT  -Name "后端 Spring Boot"
Stop-PortProcess -Port $ADMIN_PORT    -Name "管理端 Vite"
Stop-PortProcess -Port $FRONTEND_PORT -Name "用户端 Vite"
Write-Info "所有端口可用 ✓"

# ------------------------------------------
# [5/7] 启动所有服务
# ------------------------------------------
Write-Step "[5/7] 🚀 启动所有服务..."

if (-not (Test-Path $LOG_DIR)) { New-Item -ItemType Directory -Path $LOG_DIR -Force | Out-Null }

# 启动后端 (在新窗口中运行，直接显示输出和报错)
Write-Info "启动后端 (Spring Boot @ :$BACKEND_PORT)..."
$backendDir = Join-Path $PROJECT_DIR "backend"
$backendProc = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/k", "title 🔴 后端服务 (Spring Boot) && cd /d `"$backendDir`" && mvn spring-boot:run 2>&1 | tee `"$LOG_DIR\backend.log`"" `
    -PassThru
$script:StartedProcesses += $backendProc

# 启动管理端
Write-Info "启动管理端 (Vite @ :$ADMIN_PORT)..."
$adminDir = Join-Path $PROJECT_DIR "admin"
$adminProc = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/k", "title 🟢 管理端 (Admin) && cd /d `"$adminDir`" && pnpm dev 2>&1 | tee `"$LOG_DIR\admin.log`"" `
    -PassThru
$script:StartedProcesses += $adminProc

# 启动用户端
Write-Info "启动用户端 (Vite @ :$FRONTEND_PORT)..."
$frontendDir = Join-Path $PROJECT_DIR "frontend"
$frontendProc = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/k", "title 🔵 用户端 (Frontend) && cd /d `"$frontendDir`" && pnpm dev 2>&1 | tee `"$LOG_DIR\frontend.log`"" `
    -PassThru
$script:StartedProcesses += $frontendProc

# ------------------------------------------
# [6/7] 等待服务就绪
# ------------------------------------------
Write-Step "[6/7] ⏳ 等待服务就绪..."

$ok1 = Wait-Port -Port $BACKEND_PORT  -Name "后端"     -Timeout 120
$ok2 = Wait-Port -Port $ADMIN_PORT    -Name "管理端"   -Timeout 60
$ok3 = Wait-Port -Port $FRONTEND_PORT -Name "用户端"   -Timeout 60

if (-not ($ok1 -and $ok2 -and $ok3)) {
    Write-Err "部分服务启动失败，请检查各窗口中的报错信息"
}

# ------------------------------------------
# [7/7] 启动完成
# ------------------------------------------
Write-Step "[7/7] ✅ 启动完成！"

Write-Host ""
Write-Host "  ╔═══════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "  ║      🍲 火锅店管理系统 - 所有服务已启动       ║" -ForegroundColor Green
Write-Host "  ╚═══════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "  📊 管理端:   " -NoNewline; Write-Host "http://localhost:$ADMIN_PORT" -ForegroundColor Blue
Write-Host "  🌐 用户端:   " -NoNewline; Write-Host "http://localhost:$FRONTEND_PORT" -ForegroundColor Blue
Write-Host "  ⚙️  后端API:  " -NoNewline; Write-Host "http://localhost:$BACKEND_PORT" -ForegroundColor Blue
Write-Host ""
Write-Host "  ═══ 管理端测试账号 ═══" -ForegroundColor Cyan
Write-Host "  ┌──────────┬────────────┬────────┐"
Write-Host "  │   角色   │   用户名   │  密码  │"
Write-Host "  ├──────────┼────────────┼────────┤"
Write-Host "  │ 🔴 店长  │ admin      │ 123456 │"
Write-Host "  │ 🟢 收银员│ cashier1   │ 123456 │"
Write-Host "  │ 🟡 后厨  │ kitchen1   │ 123456 │"
Write-Host "  │ 🔵 库管员│ inventory1 │ 123456 │"
Write-Host "  └──────────┴────────────┴────────┘"
Write-Host ""
Write-Host "  ═══ 用户端会员测试账号 ═══" -ForegroundColor Cyan
Write-Host "  ┌──────────┬─────────────┬────────┐"
Write-Host "  │   姓名   │   手机号    │  密码  │"
Write-Host "  ├──────────┼─────────────┼────────┤"
Write-Host "  │ 刘晓红   │ 18600001001 │ 001001 │"
Write-Host "  │ 张明     │ 18600001002 │ 001002 │"
Write-Host "  │ 王丽     │ 18600001003 │ 001003 │"
Write-Host "  └──────────┴─────────────┴────────┘"
Write-Host "  提示: 会员密码为手机号后6位" -ForegroundColor Yellow
Write-Host ""
Write-Host "  💡 关闭此窗口或运行 stop.bat 停止所有服务" -ForegroundColor Yellow
Write-Host "  💡 各服务窗口会直接显示运行日志和报错信息" -ForegroundColor Yellow
Write-Host ""
Write-Host "  按任意键退出此窗口（服务将继续在后台运行）..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
