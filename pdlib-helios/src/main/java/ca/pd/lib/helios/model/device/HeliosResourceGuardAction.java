package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosResourceGuardAction(
    @JsonProperty("at_ms") long atMs,
    HeliosResourceGuardActionKind kind,
    @JsonProperty("stream_id") UUID streamId,
    String alias,
    double score,
    String reason,
    @JsonProperty("mem_available_kb") Long memAvailableKb) {}
