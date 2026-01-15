package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosRobotDimensions(
    @JsonProperty("width_m") double widthM,
    @JsonProperty("length_m") double lengthM,
    @JsonProperty("bumper_height_m") double bumperHeightM,
    @JsonProperty("bumper_thickness_m") double bumperThicknessM,
    @JsonProperty("ground_clearance_m") double groundClearanceM) {}
