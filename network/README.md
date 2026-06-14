# Detonation network isolation

The detonation guest runs real malware, so it must never touch the real internet or the operator's
LAN (threat model boundary 4). This directory documents the isolated-lab topology and the fake-service
host that lets malware "phone home" into a sink we control, so we can observe C2/DNS behaviour without
any real egress.

## Topology

```
                 host-only / private vmnet (no NAT, no bridge)
   +----------------------------+        +-------------------------------+
   |   Detonation guest (Win)   |  --->  |   Fake-net host (Linux VM)    |
   |   gateway + DNS = fakenet  |        |   INetSim: dns/http/https/... |
   +----------------------------+        +-------------------------------+
                                                  (no uplink to internet)
```

- The guest's only network is a VMware **host-only / private** vmnet — no NAT, no bridged adapter.
- A small Linux VM on the same vmnet runs INetSim and acts as the guest's **default gateway and DNS**.
- INetSim answers every DNS query with its own address and emulates HTTP/HTTPS/SMTP/etc., so the
  malware's network behaviour is captured while real egress is impossible.

## Setup (one-time)

1. In VMware Fusion, set both VMs to the same custom **private** network (no "Share with my Mac" /
   NAT, no Bridged).
2. On the fake-net VM, install INetSim and use `inetsim.conf` here (set `service_bind_address` and
   `dns_default_ip` to that VM's address). Start it: `sudo inetsim --conf inetsim.conf`.
3. In the guest, set the static IP / gateway / DNS to the fake-net VM's address.
4. Verify isolation from inside the guest before capturing the `clean` snapshot:
   ```powershell
   .\vm_scripts\verify_isolation.ps1 -FakeNetIp 10.10.10.2
   ```
   All checks must pass (gateway+DNS point at the sink, real external IPs are unreachable).

## Why this matters in review

Running malware without containment is the single scariest thing about this platform. Pairing the
snapshot auto-revert (state isolation) with INetSim (network isolation) is what makes detonation
defensible — and `verify_isolation.ps1` turns "we think it's isolated" into a check that runs before
every baseline snapshot.
