package ca.pd.lib.helios.localization;

import java.util.List;

public record HeliosLocalizationSolverConfig(
    String id,
    String name,
    String mode,
    List<HeliosPoseSpace> outputSpaces,
    List<String> sourceIds,
    String color) {}

