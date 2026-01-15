package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosCpuCoreMetrics(
    int id,
    String name,
    float pct,
    @JsonProperty("freq_mhz") long freqMhz) {}
