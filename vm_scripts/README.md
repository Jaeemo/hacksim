# Guest VM scripts — detonation telemetry

These run **inside the Windows detonation guest**. They turn the VM from a screen you watch over
noVNC into an instrumented sandbox that reports what the malware actually did, which the backend then
maps to MITRE ATT&CK (see `docs/THREAT_MODEL.md` and the backend telemetry endpoints).

## Pipeline

```
Sysmon (in guest)  ->  ship_telemetry.ps1  ->  POST /api/runs/{runId}/telemetry  ->  backend classifies -> GET /api/runs/{runId}/report
```

The backend, not the guest, assigns ATT&CK techniques, so a compromised guest cannot forge the
analysis verdict.

## One-time guest setup (then capture the `clean` snapshot)

1. Download Sysmon (Sysinternals) into the guest.
2. Install with the bundled config, elevated:
   ```powershell
   .\sysmon64.exe -accepteula -i sysmon-config.xml
   ```
3. Log in as the detonation user (so `runProgramInGuest` has a session).
4. In VMware Fusion, take a snapshot named **`clean`** — this is the baseline the backend reverts to
   before every run (`vmware.snapshot.name`).

## Per-detonation usage

1. Trigger a detonation; note the `runId` (from the audit log at `GET /api/runs`).
2. After the sample has run, ship the telemetry:
   ```powershell
   .\ship_telemetry.ps1 -RunId 42 -BackendBaseUrl http://<host-ip>:5001
   ```
3. View the behavioural report at `GET /api/runs/42/report` (or the frontend dashboard).

## Event mapping

| Sysmon event | ID | Normalised type |
|--------------|----|-----------------|
| ProcessCreate | 1 | `PROCESS_CREATE` |
| NetworkConnect | 3 | `NETWORK_CONNECT` |
| FileCreate | 11 | `FILE_WRITE` |
| RegistryEvent (SetValue) | 13 | `REGISTRY_SET` |
| DnsQuery | 22 | `DNS_QUERY` |

## Known simplification

The `runId` is passed manually today. A natural next step is for the backend to inject the current
`runId` into the guest when it launches the sample (e.g. via a dropped file or guest env var) so the
shipper is fully automated.
