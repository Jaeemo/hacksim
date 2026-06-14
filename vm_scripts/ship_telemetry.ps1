<#
.SYNOPSIS
    Collects Sysmon telemetry from the detonation guest and ships it to the Hacksim backend.

.DESCRIPTION
    Reads events from the Microsoft-Windows-Sysmon/Operational log, normalises the five event
    types the backend understands, and POSTs them as JSON to /api/runs/{runId}/telemetry. The
    backend performs the MITRE ATT&CK classification, so this script only reports raw behaviour.

.PARAMETER RunId
    The detonation run id to attach telemetry to (from the /api/start-simulation response or the
    audit log at GET /api/runs).

.PARAMETER BackendBaseUrl
    Base URL of the backend, e.g. http://192.168.1.10:5001 (the host running Spring Boot).

.PARAMETER SinceMinutes
    Only ship events from the last N minutes (default 15), so reruns don't resend old events.

.EXAMPLE
    .\ship_telemetry.ps1 -RunId 42 -BackendBaseUrl http://192.168.1.10:5001
#>
param(
    [Parameter(Mandatory = $true)] [long]   $RunId,
    [Parameter(Mandatory = $true)] [string] $BackendBaseUrl,
    [int] $SinceMinutes = 15
)

$ErrorActionPreference = 'Stop'

function Get-Field {
    param([xml]$Xml, [string]$Name)
    $node = $Xml.Event.EventData.Data | Where-Object { $_.Name -eq $Name }
    if ($node) { return [string]$node.'#text' }
    return $null
}

function ConvertTo-NormalisedEvent {
    param($WinEvent)

    $xml = [xml]$WinEvent.ToXml()
    $occurredAt = $WinEvent.TimeCreated.ToUniversalTime().ToString('o')
    $image = Get-Field $xml 'Image'

    switch ($WinEvent.Id) {
        1 { return [pscustomobject]@{ eventType = 'PROCESS_CREATE';  occurredAt = $occurredAt; actor = $image; target = (Get-Field $xml 'CommandLine'); detail = ('parent: ' + (Get-Field $xml 'ParentImage')) } }
        3 {
            $dest = Get-Field $xml 'DestinationIp'
            $port = Get-Field $xml 'DestinationPort'
            return [pscustomobject]@{ eventType = 'NETWORK_CONNECT'; occurredAt = $occurredAt; actor = $image; target = "$dest`:$port"; detail = (Get-Field $xml 'Protocol') }
        }
        11 { return [pscustomobject]@{ eventType = 'FILE_WRITE';     occurredAt = $occurredAt; actor = $image; target = (Get-Field $xml 'TargetFilename'); detail = $null } }
        13 { return [pscustomobject]@{ eventType = 'REGISTRY_SET';   occurredAt = $occurredAt; actor = $image; target = (Get-Field $xml 'TargetObject');   detail = (Get-Field $xml 'Details') } }
        22 { return [pscustomobject]@{ eventType = 'DNS_QUERY';      occurredAt = $occurredAt; actor = $image; target = (Get-Field $xml 'QueryName');      detail = (Get-Field $xml 'QueryResults') } }
        default { return $null }
    }
}

$startTime = (Get-Date).AddMinutes(-$SinceMinutes)
$filter = @{ LogName = 'Microsoft-Windows-Sysmon/Operational'; Id = 1, 3, 11, 13, 22; StartTime = $startTime }

$raw = Get-WinEvent -FilterHashtable $filter -ErrorAction SilentlyContinue
if (-not $raw) {
    Write-Host 'No Sysmon events found in the selected window. Is Sysmon installed and was the sample detonated?'
    return
}

$events = $raw | ForEach-Object { ConvertTo-NormalisedEvent $_ } | Where-Object { $_ -ne $null }
if (-not $events) {
    Write-Host 'No matching events to ship.'
    return
}

$json = ConvertTo-Json -InputObject $events -Depth 5
if (@($events).Count -eq 1) { $json = "[$json]" }   # Windows PowerShell collapses single-item arrays

$uri = "$BackendBaseUrl/api/runs/$RunId/telemetry"
Write-Host "Shipping $((@($events)).Count) events to $uri ..."
$response = Invoke-RestMethod -Uri $uri -Method Post -ContentType 'application/json; charset=utf-8' -Body $json
Write-Host "Backend response: $($response.status) - $($response.message)"
