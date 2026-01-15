package ca.pd.lib.helios.localization;

import java.util.List;

public record HeliosLocalizationConfig(String activeProfileId, List<HeliosLocalizationProfile> profiles) {}

