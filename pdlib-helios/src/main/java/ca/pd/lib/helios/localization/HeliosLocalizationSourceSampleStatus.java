package ca.pd.lib.helios.localization;

public record HeliosLocalizationSourceSampleStatus(
    String sourceId,
    String streamId,
    String outputKey,
    String cameraUid,
    int detections,
    double pollMs,
    Double tagSize,
    String error) {}

