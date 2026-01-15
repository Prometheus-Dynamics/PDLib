package ca.pd.lib.helios.localization;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;

/** Vision pose estimate suitable for {@code SwerveDrivePoseEstimator.addVisionMeasurement}. */
public record HeliosPoseEstimate2d(Pose2d pose, double timestampSeconds, Matrix<N3, N1> stdDevs, HeliosLatencyMetrics latency) {}
