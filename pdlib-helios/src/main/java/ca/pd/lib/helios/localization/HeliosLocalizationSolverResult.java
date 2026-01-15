package ca.pd.lib.helios.localization;

import java.util.List;

public record HeliosLocalizationSolverResult(
    String id,
    String name,
    String mode,
    List<HeliosPoseSpace> outputSpaces,
    HeliosLocalizationSolverOutputs outputs,
    List<String> errors) {}

