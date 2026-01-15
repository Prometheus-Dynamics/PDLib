package ca.pd.lib.helios.localization;

import java.util.List;

public record HeliosLocalizationSolverOutputs(
    List<HeliosLocalizationDetectionPose> tagInCamera,
    List<HeliosLocalizationDetectionPose> cameraInTag,
    List<HeliosLocalizationDetectionPose> tagInRobot,
    List<HeliosLocalizationDetectionPose> robotInTag,
    List<HeliosLocalizationSourcePose> cameraInField,
    HeliosLocalizationSolverPose robotInField) {}

