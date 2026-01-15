package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosLogSource(
    String id,
    String label,
    String group,
    HeliosLogSourceKind kind,
    String unit,
    String path,
    boolean important,
    HeliosSystemdUnitStatus status) {}
