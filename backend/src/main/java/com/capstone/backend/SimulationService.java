package com.capstone.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimulationService {

    private static final Logger logger = LoggerFactory.getLogger(SimulationService.class);
    private static final String WINDOWS_CMD = "C:\\Windows\\System32\\cmd.exe";

    private final VmwareProperties vmwareProperties;

    public SimulationService(VmwareProperties vmwareProperties) {
        this.vmwareProperties = vmwareProperties;
    }

    /**
     * Detonates a malware shortcut inside the guest VM. When snapshot revert is enabled the VM is
     * first rolled back to a known-clean state so every run starts from the same baseline and no
     * infection persists between runs. {@code settleDelayMs} gives the reverted guest time to finish
     * auto-login before VMware Tools commands are issued.
     */
    public void runShortcut(String shortcutPath, long settleDelayMs) throws IOException, InterruptedException {
        ensureGuestPasswordIsConfigured();

        if (vmwareProperties.getSnapshot().isRevertBeforeRun()) {
            revertToCleanSnapshot();
            ensureGuestIsRunning();
            settle(settleDelayMs);
        }

        logger.info("Running vmrun command for guest user: {}", vmwareProperties.getGuest().getUsername());
        String output = executeVmrun(buildRunProgramCommand(shortcutPath), "runProgramInGuest");
        logger.info("vmrun runProgramInGuest succeeded: {}", output.trim());
    }

    private void revertToCleanSnapshot() throws IOException, InterruptedException {
        String snapshotName = vmwareProperties.getSnapshot().getName();

        if (snapshotName == null || snapshotName.isBlank()) {
            throw new IllegalStateException("VMware snapshot name is not configured. Set VMWARE_SNAPSHOT_NAME.");
        }

        List<String> command = baseVmrunCommand();
        command.add("revertToSnapshot");
        command.add(vmwareProperties.getGuest().getVmxPath());
        command.add(snapshotName);

        logger.info("Reverting guest VM to clean snapshot '{}'.", snapshotName);
        executeVmrun(command, "revertToSnapshot");
    }

    private void ensureGuestIsRunning() throws IOException, InterruptedException {
        if (isGuestRunning()) {
            logger.info("Guest VM already running after revert.");
            return;
        }

        List<String> command = baseVmrunCommand();
        command.add("start");
        command.add(vmwareProperties.getGuest().getVmxPath());
        command.add("nogui");

        logger.info("Starting guest VM after revert.");
        executeVmrun(command, "start");
    }

    private boolean isGuestRunning() throws IOException, InterruptedException {
        List<String> command = baseVmrunCommand();
        command.add("list");

        String output = executeVmrun(command, "list");
        return output.contains(vmwareProperties.getGuest().getVmxPath());
    }

    private List<String> buildRunProgramCommand(String shortcutPath) {
        VmwareProperties.Guest guest = vmwareProperties.getGuest();
        String programArgs = "/c start \"\" \"" + shortcutPath + "\"";

        List<String> command = baseVmrunCommand();
        command.add("-gu");
        command.add(guest.getUsername());
        command.add("-gp");
        command.add(guest.getPassword());
        command.add("runProgramInGuest");
        command.add(guest.getVmxPath());
        command.add("-interactive");
        command.add(WINDOWS_CMD);
        command.add(programArgs);
        return command;
    }

    private List<String> baseVmrunCommand() {
        List<String> command = new ArrayList<>();
        command.add(vmwareProperties.getRunPath());
        command.add("-T");
        command.add("fusion");
        return command;
    }

    private String executeVmrun(List<String> command, String action) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        String output = readProcessOutput(process);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            logger.error("vmrun {} failed (exit code {}). Output: {}", action, exitCode, output.trim());
            throw new IllegalStateException("vmrun " + action + " failed (exit code " + exitCode + "): " + output.trim());
        }

        return output;
    }

    private void settle(long delayMs) throws InterruptedException {
        if (delayMs > 0) {
            Thread.sleep(delayMs);
        }
    }

    private String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        return output.toString();
    }

    private void ensureGuestPasswordIsConfigured() {
        String password = vmwareProperties.getGuest().getPassword();

        if (password == null || password.isBlank()) {
            throw new IllegalStateException("VMware guest password is not configured. Set VMWARE_GUEST_PASSWORD.");
        }
    }
}
