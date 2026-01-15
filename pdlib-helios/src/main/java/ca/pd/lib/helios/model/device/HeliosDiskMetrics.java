package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosDiskMetrics(
    String mount,
    @JsonProperty("total_bytes") long totalBytes,
    @JsonProperty("available_bytes") long availableBytes) {}
