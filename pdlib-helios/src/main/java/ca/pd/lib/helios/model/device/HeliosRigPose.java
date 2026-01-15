package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosRigPose(
    HeliosPoseVector translation,
    HeliosPoseRotation rotation,
    @JsonProperty("updated_at") String updatedAt) {}
