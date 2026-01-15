package ca.pd.lib.helios.examples.pose;

import ca.pd.lib.helios.HeliOS;
import ca.pd.lib.helios.localization.HeliosPoseEstimate2d;
import edu.wpi.first.math.estimator.DifferentialDrivePoseEstimator;
import edu.wpi.first.math.geometry.Rotation2d;
import java.util.Optional;

/**
 * Example: feed HeliOS localization into a WPILib {@link DifferentialDrivePoseEstimator}.
 *
 * <p>This is intentionally minimal; teams should tune std-dev heuristics and choose an appropriate
 * localization profile/solver for their pipeline.
 */
public final class HeliosDifferentialPoseEstimatorExample {
  private HeliosDifferentialPoseEstimatorExample() {}

  public static void addVisionMeasurement(
      HeliOS cam, DifferentialDrivePoseEstimator estimator, String profileId) throws Exception {
    HeliosPoseEstimate2d estimate = estimateFromHelios(cam, profileId).orElse(null);
    if (estimate == null) return;

    estimator.addVisionMeasurement(estimate.pose(), estimate.timestampSeconds(), estimate.stdDevs());
  }

  public static void addVisionMeasurement(HeliOS cam, DifferentialDrivePoseEstimator estimator) throws Exception {
    addVisionMeasurement(cam, estimator, null);
  }

  /**
   * Typical periodic integration:
   * 1) update odometry from sensors
   * 2) fuse HeliOS localization when available
   */
  public static void updateAndFuseVision(
      HeliOS cam,
      DifferentialDrivePoseEstimator estimator,
      Rotation2d gyroAngle,
      double leftDistanceMeters,
      double rightDistanceMeters,
      String profileId)
      throws Exception {
    estimator.update(gyroAngle, leftDistanceMeters, rightDistanceMeters);
    addVisionMeasurement(cam, estimator, profileId);
  }

  public static void updateAndFuseVision(
      HeliOS cam,
      DifferentialDrivePoseEstimator estimator,
      Rotation2d gyroAngle,
      double leftDistanceMeters,
      double rightDistanceMeters)
      throws Exception {
    updateAndFuseVision(cam, estimator, gyroAngle, leftDistanceMeters, rightDistanceMeters, null);
  }

  private static Optional<HeliosPoseEstimate2d> estimateFromHelios(HeliOS cam, String profileId) throws Exception {
    if (profileId == null || profileId.isBlank()) {
      return cam.localization().fieldPoseBlueEstimate();
    }
    return cam.localization(profileId).fieldPoseBlueEstimate();
  }
}
