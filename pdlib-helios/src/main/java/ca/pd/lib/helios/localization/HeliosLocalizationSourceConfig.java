package ca.pd.lib.helios.localization;

public record HeliosLocalizationSourceConfig(
    String id,
    String streamId,
    String outputKey,
    String cameraUid,
    HeliosPoseSpace poseSpace,
    String inputKey,
    boolean enabled,
    float weight) {}

