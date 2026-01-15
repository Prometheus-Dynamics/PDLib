package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosSystemdUnitStatus(
    @JsonProperty("active_state") String activeState,
    @JsonProperty("sub_state") String subState,
    @JsonProperty("unit_file_state") String unitFileState,
    String description,
    @JsonProperty("fragment_path") String fragmentPath,
    @JsonProperty("main_pid") Integer mainPid,
    @JsonProperty("exec_main_status") Integer execMainStatus) {}
