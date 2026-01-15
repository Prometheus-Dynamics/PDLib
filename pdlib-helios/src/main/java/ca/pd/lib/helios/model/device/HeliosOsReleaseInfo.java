package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosOsReleaseInfo(
    @JsonProperty("version_id") String versionId,
    @JsonProperty("build_id") String buildId,
    @JsonProperty("pretty_name") String prettyName) {}
