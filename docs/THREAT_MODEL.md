# Hacksim — Threat Model

> Status: living document (skeleton). Last updated: 2026-06-14.
> Methodology: STRIDE, scoped to the malware-detonation platform.

Hacksim intentionally executes **real malware** inside a VM so users can observe its behaviour.
That makes the platform itself a high-value, high-risk target: a flaw here doesn't just leak data,
it can let attacker-controlled code escape the sandbox onto the host or the operator's network.
This document captures the assets, trust boundaries, and threats so the design can be reasoned about
deliberately rather than by accident.

---

## 1. System overview

| Component | Tech | Role |
|-----------|------|------|
| Web UI | React (CRA) | Learning content, safe in-browser simulation, triggers real detonation |
| Backend | Spring Boot, Java 17 | Exposes `POST /api/start-simulation/{type}`, orchestrates the guest VM via `vmrun` |
| Detonation VM | VMware Fusion guest (Windows) | Where the actual malware shortcut is executed |
| noVNC | Docker (websockify) | Streams the guest screen to the browser (`:8080` → VNC `:5901`) |
| Diagnostic agent | (planned) | Endpoint posture self-assessment, reports back to backend |

Detonation flow: UI → `POST /api/start-simulation/{type}` → backend resolves a configured shortcut →
`vmrun revertToSnapshot` (clean baseline) → `vmrun start` → settle delay → `vmrun runProgramInGuest`
launches the malware → user watches via noVNC.

---

## 2. Assets (what we are protecting)

1. **The operator's host machine** — VMware Fusion runs on it; a guest escape compromises it directly.
2. **The operator's / lab network** — malware may attempt lateral movement or reach external C2.
3. **VMware guest credentials** (`vmware.guest.username` / `password`) — used by `runProgramInGuest`.
4. **The detonation capability itself** — the ability to run arbitrary code in the guest is a weapon;
   unauthorized access to the trigger endpoint is itself a critical threat.
5. **Integrity of analysis output** (Phase 1) — telemetry/reports must reflect what actually ran.

---

## 3. Trust boundaries

![Hacksim data-flow diagram with trust boundaries](dfd.svg)

The DFD above shows the same crossings as a level-1 data-flow diagram; each arrow that crosses a
dashed boundary line is an entry point an attacker can target.

```
[ Browser ] --(1)--> [ Backend API ] --(2)--> [ vmrun / host OS ] --(3)--> [ Guest VM ] --(4)--> [ Network ]
```

- **(1) Browser ↔ Backend** — untrusted client over HTTP/CORS. Anyone who can reach the port.
- **(2) Backend ↔ Host OS** — backend spawns host processes (`vmrun`) with stored guest credentials.
- **(3) Host ↔ Guest VM** — the core isolation boundary; malware lives on the far side.
- **(4) Guest ↔ Network** — where malware tries to call home or spread.

---

## 4. STRIDE analysis

Legend for mitigation status: ✅ implemented · 🟡 partial · ⬜ planned.

### Boundary (1): Browser ↔ Backend

| STRIDE | Threat | Mitigation | Status |
|--------|--------|------------|--------|
| Spoofing | Anyone on the network calls the trigger endpoint — no authentication today | Add authn (token/session) on `/api/**`; bind backend to localhost in single-host setups | ⬜ |
| Tampering | Malicious `simulationType` path value | Type is mapped against a fixed allowlist of configured shortcuts; unknown → 404 | ✅ |
| Repudiation | No record of who triggered what detonation | Add audit log (who/when/which scenario) | ⬜ |
| Information disclosure | Verbose error messages return raw `vmrun` output to the client | Return generic errors to client, keep detail in server logs; add `@ControllerAdvice` | 🟡 |
| Denial of service | Unauthenticated endpoint can be hammered; each call blocks a request thread | Rate limiting + move detonation to an async job queue (Phase 5) | ⬜ |
| Elevation of privilege | CORS `allowCredentials(true)` with configurable origins | Origins are allowlisted via config (no `*`); review before exposing publicly | 🟡 |

### Boundary (2): Backend ↔ Host OS

| STRIDE | Threat | Mitigation | Status |
|--------|--------|------------|--------|
| Tampering | Command/argument injection into the spawned `vmrun` process | `ProcessBuilder` is invoked with an argument **list** (no shell); the shortcut path comes only from server-side config, never client input | ✅ |
| Information disclosure | Guest password is a config value, could leak via logs/env dumps | Supplied via env var, never logged; move to a secrets manager | 🟡 |
| Elevation of privilege | Backend process privileges = blast radius if backend is compromised | Run backend as a least-privilege user; document required permissions | ⬜ |

### Boundary (3): Host ↔ Guest VM  *(the critical one)*

| STRIDE | Threat | Mitigation | Status |
|--------|--------|------------|--------|
| Tampering | Malware persists across runs, contaminating later detonations | **Auto-revert to a clean snapshot before every run** (`vmrun revertToSnapshot`) | ✅ |
| Elevation of privilege | VM escape / hypervisor exploit reaches the host | Keep VMware patched; disable shared folders, clipboard, drag-and-drop; no host mounts | ⬜ |
| Information disclosure | Shared folders expose host files to malware | Disable host↔guest shared folders entirely | ⬜ |
| Spoofing | Snapshot is not actually clean (taken from an infected state) | Document a verified procedure for capturing the `clean` snapshot | ⬜ |

### Boundary (4): Guest ↔ Network

| STRIDE | Threat | Mitigation | Status |
|--------|--------|------------|--------|
| Information disclosure | Malware exfiltrates data to real external C2 | Host-only vmnet + INetSim sink answers DNS/HTTP locally — see `network/` | 🟡 |
| Denial of service / spread | Worm-type samples scan and infect the LAN | No bridged/NAT adapter; isolated private vmnet only (`network/README.md`) | 🟡 |
| Tampering | Malware downloads a second-stage payload from the internet | No real egress; `vm_scripts/verify_isolation.ps1` gates the baseline snapshot | 🟡 |

---

## 5. Key residual risks (today)

1. **Unauthenticated detonation trigger** — anyone able to reach the backend port can execute malware
   in the guest. Highest-priority gap. *(Phase 4)*
2. **Network isolation depends on operator setup** — the isolated-vmnet + INetSim design and a
   pre-snapshot verification script ship in `network/`, but isolation is only real once the lab is
   wired that way; `verify_isolation.ps1` is the gate that proves it. *(Phase 2 — design done)*
3. **Host-coupled, hard to contain** — runs on the operator's personal machine with no documented
   hardening of the VMware host↔guest channel. *(Phase 2/5)*

---

## 6. TODO (fills in as the roadmap lands)

- [x] Data-flow diagram with explicit trust-boundary lines (DFD level 1) — `docs/dfd.svg`
- [x] Network-isolation design + INetSim config + verification script (Boundary 4) — `network/`, `vm_scripts/verify_isolation.ps1`
- [ ] Authn + audit logging design (Boundary 1)
- [ ] Documented procedure for capturing a verified-clean snapshot (Boundary 3)
- [ ] Tie detected behaviours (Phase 1 telemetry) back to the threats above
