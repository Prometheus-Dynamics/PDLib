package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosIpaStatus(List<HeliosIpaFileInfo> files) {}
