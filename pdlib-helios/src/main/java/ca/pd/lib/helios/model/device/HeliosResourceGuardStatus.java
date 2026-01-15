package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosResourceGuardStatus(
    boolean enabled,
    @JsonProperty("poll_ms") long pollMs,
    @JsonProperty("cooldown_ms") long cooldownMs,
    @JsonProperty("mem_low_kb") long memLowKb,
    @JsonProperty("mem_recover_kb") long memRecoverKb,
    @JsonProperty("last_mem_available_kb") Long lastMemAvailableKb,
    @JsonProperty("pressure_active") boolean pressureActive,
    @JsonProperty("degraded_streams") List<HeliosResourceGuardDegradedStream> degradedStreams,
    @JsonProperty("last_action") HeliosResourceGuardAction lastAction,
    @JsonProperty("recent_actions") List<HeliosResourceGuardAction> recentActions) {}
