package ca.pd.lib.helios;

import ca.pd.lib.helios.localization.HeliosLocalizationProfile;
import ca.pd.lib.helios.localization.HeliosLocalizationSolveResponse;
import ca.pd.lib.helios.localization.HeliosPoseEstimate2d;
import edu.wpi.first.math.geometry.Pose2d;
import java.util.Objects;
import java.util.Optional;

/** Read-only wrapper around localization profile + solve endpoints. */
public final class HeliOSLocalization {
  private final HeliOS helios;
  private final String profileId; // nullable for backend default

  HeliOSLocalization(HeliOS helios, String profileId) {
    this.helios = Objects.requireNonNull(helios, "helios");
    this.profileId = profileId;
  }

  /** Profile id, or {@code null} when using backend default profile selection. */
  public String profileId() {
    return profileId;
  }

  public Optional<HeliosLocalizationProfile> profile() throws Exception {
    if (profileId == null || profileId.isBlank()) {
      return Optional.empty();
    }
    for (HeliosLocalizationProfile profile : helios.localizations()) {
      if (profile != null && profileId.equals(profile.id())) {
        return Optional.of(profile);
      }
    }
    return Optional.empty();
  }

  public HeliosLocalizationSolveResponse solve() throws Exception {
    return helios.localizationClient().solve(profileId);
  }

  public Optional<Pose2d> fieldPoseBlue() throws Exception {
    return fieldPoseBlueEstimate().map(HeliosPoseEstimate2d::pose);
  }

  public Optional<HeliosPoseEstimate2d> fieldPoseBlueEstimate() throws Exception {
    return helios.localizationClient().getRobotInFieldEstimate(profileId);
  }
}
