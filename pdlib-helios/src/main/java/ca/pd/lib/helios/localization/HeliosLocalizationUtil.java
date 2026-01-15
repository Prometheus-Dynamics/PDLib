package ca.pd.lib.helios.localization;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import java.util.List;

public final class HeliosLocalizationUtil {
  private HeliosLocalizationUtil() {}

  /**
   * Convert a HeliOS pose into a WPILib field-space {@link Pose2d}.
   *
   * <p>HeliOS localization uses a viewer-style frame where:
   * - +X is left
   * - +Y is up
   * - +Z is forward
   *
   * <p>WPILib uses:
   * - +X is forward
   * - +Y is left
   * - +Z is up
   *
   * <p>So: {@code wpilib_x = helios_z}, {@code wpilib_y = helios_x}, and yaw is preserved.
   */
  public static Pose2d toWpilibPose2dField(HeliosLocalizationPose pose) {
    double x = pose.translation().z();
    double y = pose.translation().x();
    Rotation2d rot = Rotation2d.fromDegrees(pose.rotation().yaw());
    return new Pose2d(x, y, rot);
  }

  public static double averageTagDistanceMeters(List<HeliosLocalizationDetectionPose> tagInCamera) {
    if (tagInCamera == null || tagInCamera.isEmpty()) return Double.NaN;
    double sum = 0.0;
    int n = 0;
    for (HeliosLocalizationDetectionPose det : tagInCamera) {
      if (det == null || det.pose() == null) continue;
      HeliosLocalizationVector t = det.pose().translation();
      double d = Math.sqrt(t.x() * t.x() + t.y() * t.y() + t.z() * t.z());
      if (!Double.isFinite(d)) continue;
      sum += d;
      n++;
    }
    return n == 0 ? Double.NaN : (sum / n);
  }

  /**
   * Best-effort estimate of field-space measurement standard deviations for (x, y, heading).
   *
   * <p>HeliOS does not currently provide covariance directly; this is a heuristic meant to
   * be "reasonable by default" and easy to override.
   */
  public static Matrix<N3, N1> estimateFieldPoseStdDevs(int tagCount, double avgDistanceMeters) {
    int n = Math.max(0, tagCount);
    double dist = Double.isFinite(avgDistanceMeters) ? Math.max(0.0, avgDistanceMeters) : 0.0;

    // Tuned to be conservative by default:
    // - more tags => lower std
    // - farther tags => higher std
    double tagsFactor = n <= 0 ? 3.0 : 1.0 / Math.sqrt(n);
    double distFactor = 1.0 + (dist / 2.5);

    double xy = 0.35 * distFactor * tagsFactor; // meters
    double theta = Math.toRadians(15.0) * distFactor * tagsFactor; // radians
    return VecBuilder.fill(xy, xy, theta);
  }
}

