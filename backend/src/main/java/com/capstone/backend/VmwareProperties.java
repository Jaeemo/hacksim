package com.capstone.backend;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vmware")
public class VmwareProperties {

    private String runPath;
    private Guest guest = new Guest();
    private Snapshot snapshot = new Snapshot();

    public String getRunPath() {
        return runPath;
    }

    public void setRunPath(String runPath) {
        this.runPath = runPath;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public static class Snapshot {
        private String name = "clean";
        private boolean revertBeforeRun = true;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isRevertBeforeRun() {
            return revertBeforeRun;
        }

        public void setRevertBeforeRun(boolean revertBeforeRun) {
            this.revertBeforeRun = revertBeforeRun;
        }
    }

    public static class Guest {
        private String vmxPath;
        private String username;
        private String password;

        public String getVmxPath() {
            return vmxPath;
        }

        public void setVmxPath(String vmxPath) {
            this.vmxPath = vmxPath;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
