package ca.pd.lib.helios.localization;

import java.util.List;

public record HeliosLocalizationSolveResponse(
    String profileId,
    List<HeliosLocalizationSolverResult> solvers,
    List<HeliosLocalizationSourceSampleStatus> sources) {}

