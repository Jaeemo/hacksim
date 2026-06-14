<#
.SYNOPSIS
    Verifies the detonation guest is network-isolated before a baseline snapshot is captured.

.DESCRIPTION
    Confirms three things from inside the guest:
      1. the default gateway is the fake-net sink (not a real router),
      2. DNS resolves arbitrary external names to the sink (INetSim is answering),
      3. a known real external IP is NOT reachable (no real egress).
    Exits non-zero if any check fails, so it can gate snapshot capture in a script.

.PARAMETER FakeNetIp
    The fake-net / INetSim host address on the isolated vmnet (gateway + DNS).

.EXAMPLE
    .\verify_isolation.ps1 -FakeNetIp 10.10.10.2
#>
param(
    [Parameter(Mandatory = $true)] [string] $FakeNetIp,
    [string] $RealProbeIp = '9.9.9.9'
)

$results = @()

function Add-Result {
    param([string]$Name, [bool]$Pass, [string]$Detail)
    $script:results += [pscustomobject]@{ Check = $Name; Pass = $Pass; Detail = $Detail }
}

# 1. Default gateway must be the sink
$gateway = (Get-NetRoute -DestinationPrefix '0.0.0.0/0' -ErrorAction SilentlyContinue |
    Sort-Object RouteMetric | Select-Object -First 1).NextHop
Add-Result 'Default gateway is fake-net sink' ($gateway -eq $FakeNetIp) "gateway=$gateway"

# 2. Arbitrary external DNS must resolve to the sink (INetSim answers everything)
try {
    $resolved = (Resolve-DnsName -Name 'definitely-not-real-c2.example' -Type A -ErrorAction Stop |
        Where-Object { $_.IPAddress } | Select-Object -First 1).IPAddress
    Add-Result 'DNS redirected to sink' ($resolved -eq $FakeNetIp) "resolved=$resolved"
} catch {
    Add-Result 'DNS redirected to sink' $false "resolve failed: $($_.Exception.Message)"
}

# 3. A real external IP must be unreachable (no real egress)
$reachedReal = Test-Connection -ComputerName $RealProbeIp -Count 1 -Quiet -ErrorAction SilentlyContinue
Add-Result 'No real internet egress' (-not $reachedReal) "probe=$RealProbeIp reachable=$reachedReal"

$results | Format-Table -AutoSize

if ($results.Pass -contains $false) {
    Write-Host 'ISOLATION CHECK FAILED — do not detonate or snapshot until fixed.' -ForegroundColor Red
    exit 1
}
Write-Host 'Isolation verified.' -ForegroundColor Green
exit 0
