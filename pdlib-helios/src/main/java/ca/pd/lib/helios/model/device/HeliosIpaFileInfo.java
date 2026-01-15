package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosIpaFileInfo(
    String target,
    String path,
    boolean exists,
    Long sizeBytes,
    Long modifiedAtMs,
    String sha256,
    Long ccmCt,
    List<Double> ccm) {}
