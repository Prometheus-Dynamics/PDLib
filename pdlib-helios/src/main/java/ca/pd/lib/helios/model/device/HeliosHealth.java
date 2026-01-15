package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosHealth(
    boolean ok,
    @JsonProperty("server_time_ms") long serverTimeMs,
    @JsonProperty("uptime_ms") long uptimeMs,
    String version,
    HeliosHealthFeatures features) {}
