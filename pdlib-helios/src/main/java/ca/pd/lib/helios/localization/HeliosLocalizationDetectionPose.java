package ca.pd.lib.helios.localization;

import com.fasterxml.jackson.databind.JsonNode;

public record HeliosLocalizationDetectionPose(
    String sourceId,
    String cameraUid,
    int tagId,
    HeliosLocalizationPose pose,
    Double tagSize,
    Integer codeRotation,
    JsonNode tagBits) {}

