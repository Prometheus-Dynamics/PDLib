package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosBootloaderStatus(
    boolean supported,
    @JsonProperty("current_version") String currentVersion,
    @JsonProperty("required_version") String requiredVersion,
    @JsonProperty("needs_update") boolean needsUpdate,
    @JsonProperty("update_available") boolean updateAvailable,
    @JsonProperty("update_file") String updateFile,
    @JsonProperty("update_sig") String updateSig,
    boolean staged,
    @JsonProperty("status_message") String statusMessage) {}
