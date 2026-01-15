package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosResourceGuardDegradedStream(
    @JsonProperty("stream_id") UUID streamId,
    String alias,
    HeliosResourceGuardStage stage,
    @JsonProperty("changed_at_ms") long changedAtMs) {}
