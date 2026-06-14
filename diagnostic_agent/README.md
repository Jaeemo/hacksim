# Endpoint posture self-diagnosis agent

After experiencing the simulation, a user checks their own machine. This agent implements the eight
checks the self-diagnosis page describes, each mapped to a CIS-style control, and reports a posture
score back to the backend so results show up on the dashboard.

## Checks

| # | Check | CIS-style control |
|---|-------|-------------------|
| 1 | Firewall enabled (all profiles) | Firewall configuration |
| 2 | Defender real-time protection | Endpoint protection |
| 3 | Guest account disabled | Account policies |
| 4 | Risky listening ports (RDP/SMB) | Network exposure |
| 5 | Suspicious files in Startup | Autostart integrity |
| 6 | UAC enabled (`EnableLUA`) | Privilege controls |
| 7 | OS updates within 30 days | Patch management |
| 8 | Hosts file tampering | Host integrity |

Each check returns `PASS` / `WARN` / `FAIL`. The backend computes the score
(`PASS=1.0, WARN=0.5, FAIL=0.0`), so the number always matches the findings.

## Usage

Run elevated (admin) for accurate results:

```powershell
# Print the report locally
.\Invoke-PostureCheck.ps1

# Submit to the backend (shows on the self-diagnosis page / dashboard)
.\Invoke-PostureCheck.ps1 -BackendBaseUrl http://<host-ip>:5001
```

## API

- `POST /api/posture` — submit a report `{ hostId, os, checks[] }`; returns the stored report with score.
- `GET /api/posture` — recent reports (latest first).
