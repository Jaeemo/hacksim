<#
.SYNOPSIS
    Hacksim endpoint posture self-assessment agent.

.DESCRIPTION
    Runs eight Windows security-posture checks (the ones described on the self-diagnosis page),
    each mapped to a CIS-style control, and prints a structured report. With -BackendBaseUrl it also
    POSTs the report to /api/posture, where the backend stores it and computes the score.

    Run elevated for accurate results.

.PARAMETER BackendBaseUrl
    Optional. Base URL of the backend (e.g. http://192.168.1.10:5001) to submit the report to.

.EXAMPLE
    .\Invoke-PostureCheck.ps1
    .\Invoke-PostureCheck.ps1 -BackendBaseUrl http://192.168.1.10:5001
#>
param(
    [string] $BackendBaseUrl
)

$checks = New-Object System.Collections.Generic.List[object]

function Add-Check {
    param([string]$Id, [string]$Name, [string]$Status, [string]$Severity, [string]$Detail)
    $checks.Add([pscustomobject]@{ checkId = $Id; name = $Name; status = $Status; severity = $Severity; detail = $Detail })
}

# 1. Firewall — all profiles enabled (CIS 9.x)
try {
    $off = Get-NetFirewallProfile | Where-Object { -not $_.Enabled }
    if ($off) { Add-Check 'firewall' '방화벽 활성화' 'FAIL' 'HIGH' ("비활성 프로필: " + ($off.Name -join ', ')) }
    else { Add-Check 'firewall' '방화벽 활성화' 'PASS' 'HIGH' '모든 프로필 활성' }
} catch { Add-Check 'firewall' '방화벽 활성화' 'WARN' 'HIGH' "확인 불가: $($_.Exception.Message)" }

# 2. Antivirus / Defender real-time protection (CIS 18.9.x)
try {
    $mp = Get-MpComputerStatus -ErrorAction Stop
    if ($mp.RealTimeProtectionEnabled) { Add-Check 'antivirus' '백신 실시간 보호' 'PASS' 'HIGH' 'Defender 실시간 보호 켜짐' }
    else { Add-Check 'antivirus' '백신 실시간 보호' 'FAIL' 'HIGH' '실시간 보호 꺼짐' }
} catch { Add-Check 'antivirus' '백신 실시간 보호' 'WARN' 'HIGH' 'Defender 상태 확인 불가' }

# 3. Guest account disabled (CIS 2.3.1.x)
try {
    $guest = Get-LocalUser -Name 'Guest' -ErrorAction Stop
    if ($guest.Enabled) { Add-Check 'guest_account' 'Guest 계정 비활성화' 'FAIL' 'MEDIUM' 'Guest 계정이 활성 상태' }
    else { Add-Check 'guest_account' 'Guest 계정 비활성화' 'PASS' 'MEDIUM' 'Guest 계정 비활성' }
} catch { Add-Check 'guest_account' 'Guest 계정 비활성화' 'WARN' 'MEDIUM' 'Guest 계정 확인 불가' }

# 4. Risky listening ports (RDP 3389 / SMB 445)
try {
    $risky = Get-NetTCPConnection -State Listen -ErrorAction Stop |
        Where-Object { $_.LocalPort -in 3389, 445 } | Select-Object -ExpandProperty LocalPort -Unique
    if ($risky) { Add-Check 'open_ports' '위험 포트 노출' 'WARN' 'MEDIUM' ("수신 중: " + ($risky -join ', ')) }
    else { Add-Check 'open_ports' '위험 포트 노출' 'PASS' 'MEDIUM' '3389/445 수신 없음' }
} catch { Add-Check 'open_ports' '위험 포트 노출' 'WARN' 'MEDIUM' '포트 확인 불가' }

# 5. Suspicious executables in the Startup folder
try {
    $startup = [Environment]::GetFolderPath('Startup')
    $exes = Get-ChildItem -Path $startup -Include *.exe, *.scr, *.bat, *.vbs -Recurse -ErrorAction SilentlyContinue
    if ($exes) { Add-Check 'suspicious_files' '시작프로그램 의심 파일' 'WARN' 'MEDIUM' ("발견: " + ($exes.Name -join ', ')) }
    else { Add-Check 'suspicious_files' '시작프로그램 의심 파일' 'PASS' 'MEDIUM' '시작 폴더 깨끗함' }
} catch { Add-Check 'suspicious_files' '시작프로그램 의심 파일' 'WARN' 'MEDIUM' '확인 불가' }

# 6. UAC enabled (CIS 2.3.17.x)
try {
    $lua = (Get-ItemProperty 'HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Policies\System' -Name EnableLUA -ErrorAction Stop).EnableLUA
    if ($lua -eq 1) { Add-Check 'uac' 'UAC 활성화' 'PASS' 'HIGH' 'EnableLUA=1' }
    else { Add-Check 'uac' 'UAC 활성화' 'FAIL' 'HIGH' 'UAC 비활성' }
} catch { Add-Check 'uac' 'UAC 활성화' 'WARN' 'HIGH' 'UAC 설정 확인 불가' }

# 7. Recent OS updates (hotfix within 30 days)
try {
    $last = (Get-HotFix -ErrorAction Stop | Where-Object { $_.InstalledOn } | Sort-Object InstalledOn -Descending | Select-Object -First 1).InstalledOn
    if ($last -and $last -gt (Get-Date).AddDays(-30)) { Add-Check 'updates' '최신 보안 업데이트' 'PASS' 'MEDIUM' ("최근 업데이트: " + $last.ToString('yyyy-MM-dd')) }
    else { Add-Check 'updates' '최신 보안 업데이트' 'WARN' 'MEDIUM' '30일 내 업데이트 없음' }
} catch { Add-Check 'updates' '최신 보안 업데이트' 'WARN' 'MEDIUM' '업데이트 이력 확인 불가' }

# 8. Hosts file tampering
try {
    $hosts = Get-Content "$env:WinDir\System32\drivers\etc\hosts" -ErrorAction Stop |
        Where-Object { $_ -and -not $_.TrimStart().StartsWith('#') -and $_ -notmatch '127\.0\.0\.1|::1' }
    if ($hosts) { Add-Check 'hosts_tamper' 'Hosts 파일 변조' 'FAIL' 'HIGH' ("의심 항목 " + $hosts.Count + "건") }
    else { Add-Check 'hosts_tamper' 'Hosts 파일 변조' 'PASS' 'HIGH' '변조 흔적 없음' }
} catch { Add-Check 'hosts_tamper' 'Hosts 파일 변조' 'WARN' 'HIGH' 'hosts 확인 불가' }

$report = [pscustomobject]@{
    hostId = $env:COMPUTERNAME
    os     = (Get-CimInstance Win32_OperatingSystem -ErrorAction SilentlyContinue).Caption
    checks = $checks
}

$report.checks | Format-Table checkId, status, severity, detail -AutoSize

if ($BackendBaseUrl) {
    $json = ConvertTo-Json -InputObject $report -Depth 5
    $uri = "$BackendBaseUrl/api/posture"
    Write-Host "Submitting posture report to $uri ..."
    $resp = Invoke-RestMethod -Uri $uri -Method Post -ContentType 'application/json; charset=utf-8' -Body $json
    Write-Host ("Backend score: {0}  (report #{1})" -f $resp.score, $resp.id)
} else {
    Write-Host 'No -BackendBaseUrl given; printed report only.'
}
