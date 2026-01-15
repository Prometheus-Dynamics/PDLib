package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosIpAssignment(String address, int prefix) {}
