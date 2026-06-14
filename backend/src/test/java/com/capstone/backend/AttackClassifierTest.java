package com.capstone.backend;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AttackClassifierTest {

    private final AttackClassifier classifier = new AttackClassifier();

    @Test
    void mapsFileEncryptionToImpact() {
        Optional<AttackTechnique> technique = classifier.classify(
                TelemetryEventType.FILE_RENAME, "C:\\Temp\\evil.exe", "C:\\docs\\report.docx.locked");

        assertThat(technique).isPresent();
        assertThat(technique.get().id()).isEqualTo("T1486");
    }

    @Test
    void mapsRunKeyToAutostartPersistence() {
        Optional<AttackTechnique> technique = classifier.classify(
                TelemetryEventType.REGISTRY_SET, "C:\\Temp\\evil.exe",
                "HKLM\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\\evil");

        assertThat(technique).isPresent();
        assertThat(technique.get().id()).isEqualTo("T1547.001");
    }

    @Test
    void mapsPowerShellAndCmdToInterpreters() {
        assertThat(classifier.classify(TelemetryEventType.PROCESS_CREATE,
                "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe", "-enc ...")
                .map(AttackTechnique::id)).contains("T1059.001");

        assertThat(classifier.classify(TelemetryEventType.PROCESS_CREATE,
                "C:\\Windows\\System32\\cmd.exe", "/c start ...")
                .map(AttackTechnique::id)).contains("T1059.003");
    }

    @Test
    void mapsNetworkAndDnsToC2Comms() {
        assertThat(classifier.classify(TelemetryEventType.NETWORK_CONNECT, "evil.exe", "203.0.113.5:443")
                .map(AttackTechnique::id)).contains("T1071");

        assertThat(classifier.classify(TelemetryEventType.DNS_QUERY, "evil.exe", "c2.example.com")
                .map(AttackTechnique::id)).contains("T1071.004");
    }

    @Test
    void returnsEmptyForBenignBehaviour() {
        assertThat(classifier.classify(TelemetryEventType.FILE_WRITE, "notepad.exe", "C:\\docs\\notes.txt")).isEmpty();
        assertThat(classifier.classify(TelemetryEventType.REGISTRY_SET, "evil.exe", "HKCU\\Software\\App\\Settings")).isEmpty();
        assertThat(classifier.classify(TelemetryEventType.PROCESS_CREATE, "C:\\app\\app.exe", "")).isEmpty();
        assertThat(classifier.classify(TelemetryEventType.OTHER, "x", "y")).isEmpty();
    }
}
