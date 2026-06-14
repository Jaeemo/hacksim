package com.capstone.backend;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * Maps an observed behaviour to the MITRE ATT&CK technique it most likely represents. Rule-based and
 * deliberately small — the point is to demonstrate the static-behaviour → technique mapping that real
 * sandboxes (CAPE, Any.run) perform, not to be exhaustive. Unrecognised behaviour returns empty.
 */
@Component
public class AttackClassifier {

    public Optional<AttackTechnique> classify(TelemetryEventType eventType, String actor, String target) {
        String actorLower = lower(actor);
        String targetLower = lower(target);

        return switch (eventType) {
            case REGISTRY_SET -> classifyRegistry(targetLower);
            case FILE_WRITE, FILE_RENAME -> classifyFile(targetLower);
            case PROCESS_CREATE -> classifyProcess(actorLower);
            case NETWORK_CONNECT -> Optional.of(new AttackTechnique("T1071", "Application Layer Protocol"));
            case DNS_QUERY -> Optional.of(new AttackTechnique("T1071.004", "Application Layer Protocol: DNS"));
            case OTHER -> Optional.empty();
        };
    }

    private Optional<AttackTechnique> classifyRegistry(String target) {
        if (target.contains("\\run") || target.contains("currentversion\\run")) {
            return Optional.of(new AttackTechnique("T1547.001",
                    "Boot or Logon Autostart Execution: Registry Run Keys / Startup Folder"));
        }
        return Optional.empty();
    }

    private Optional<AttackTechnique> classifyFile(String target) {
        if (target.endsWith(".locked") || target.endsWith(".encrypted") || target.endsWith(".crypt")) {
            return Optional.of(new AttackTechnique("T1486", "Data Encrypted for Impact"));
        }
        return Optional.empty();
    }

    private Optional<AttackTechnique> classifyProcess(String actor) {
        if (actor.contains("powershell")) {
            return Optional.of(new AttackTechnique("T1059.001", "Command and Scripting Interpreter: PowerShell"));
        }
        if (actor.contains("cmd.exe")) {
            return Optional.of(new AttackTechnique("T1059.003", "Command and Scripting Interpreter: Windows Command Shell"));
        }
        return Optional.empty();
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
