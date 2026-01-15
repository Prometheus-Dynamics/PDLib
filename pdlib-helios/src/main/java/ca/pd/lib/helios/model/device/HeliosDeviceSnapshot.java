package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosDeviceSnapshot(
    String id,
    String label,
    @JsonProperty("created_by") String createdBy,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("size_bytes") long sizeBytes,
    String status) {}
