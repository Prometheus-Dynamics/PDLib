package ca.pd.lib.helios.localization;

import java.util.List;

public record HeliosLocalizationProfile(
    String id,
    String name,
    Double tagSizeM,
    String fieldMapId,
    boolean snapZToGround,
    String pipelineTemplateId,
    String color,
    boolean viewEnabled,
    List<HeliosLocalizationSourceConfig> sources,
    List<HeliosLocalizationSolverConfig> solvers) {}

