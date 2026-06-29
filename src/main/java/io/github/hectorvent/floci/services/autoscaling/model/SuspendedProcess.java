package io.github.hectorvent.floci.services.autoscaling.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuspendedProcess {

    private String processName;
    private String suspensionReason;

    public SuspendedProcess() {}

    public SuspendedProcess(String processName, String suspensionReason) {
        this.processName = processName;
        this.suspensionReason = suspensionReason;
    }

    public String getProcessName() { return processName; }
    public void setProcessName(String v) { this.processName = v; }

    public String getSuspensionReason() { return suspensionReason; }
    public void setSuspensionReason(String v) { this.suspensionReason = v; }
}
