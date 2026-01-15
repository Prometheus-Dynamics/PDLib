package ca.pd.lib.helios.vision;

import ca.pd.lib.helios.localization.HeliosLocalizationDetectionPose;
import ca.pd.lib.helios.localization.HeliosLocalizationSolveResponse;
import ca.pd.lib.helios.localization.HeliosLocalizationSolverResult;
import ca.pd.lib.helios.localization.HeliosLocalizationVector;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.List;

public final class HeliosVisionHelpers {
  private HeliosVisionHelpers() {}

  /**
   * Parse a 2D detections output.
   *
   * <p>Supports both:
   * - raw array output (from {@code /v1/streams/{id}/pipeline/outputs/{port}/sample})
   * - wrapped output (from {@code /v1/localization/streams/{id}/outputs/{output_key}})
   */
  public static List<HeliosFiducialDetection2d> parseFiducialDetections2d(JsonNode sample) {
    if (sample == null || sample.isNull()) return List.of();

    JsonNode value = sample;
    if (sample.isObject() && sample.has("value")) {
      value = sample.get("value");
    }
    if (!(value instanceof ArrayNode arr)) return List.of();

    List<HeliosFiducialDetection2d> out = new ArrayList<>(arr.size());
    for (JsonNode det : arr) {
      int id = det.path("id").asInt(-1);
      int rot = det.path("rotation").asInt(0);

      JsonNode cornersNode = det.get("corners");
      if (!(cornersNode instanceof ArrayNode cornersArr) || cornersArr.size() == 0) continue;
      List<HeliosPoint2d> corners = new ArrayList<>(cornersArr.size());
      for (JsonNode c : cornersArr) {
        corners.add(new HeliosPoint2d(c.path("x").asDouble(Double.NaN), c.path("y").asDouble(Double.NaN)));
      }
      out.add(new HeliosFiducialDetection2d(id, rot, List.copyOf(corners)));
    }
    return List.copyOf(out);
  }

  /** Return the solver's {@code tagInCamera} list for a given solver id (or first solver if null). */
  public static List<HeliosLocalizationDetectionPose> tagInCamera(HeliosLocalizationSolveResponse solve, String solverId) {
    if (solve == null || solve.solvers() == null) return List.of();
    HeliosLocalizationSolverResult picked = null;
    for (HeliosLocalizationSolverResult s : solve.solvers()) {
      if (s == null || s.outputs() == null) continue;
      if (solverId != null && !solverId.isBlank() && !solverId.equals(s.id())) continue;
      if (s.outputs().tagInCamera() != null) {
        picked = s;
        break;
      }
    }
    if (picked == null || picked.outputs().tagInCamera() == null) return List.of();
    return picked.outputs().tagInCamera();
  }

  /**
   * Compute yaw (left+) and pitch (up+) angles in degrees from a camera-space translation.
   *
   * <p>Assumes the HeliOS localization camera frame convention:
   * - +X left
   * - +Y up
   * - +Z forward
   */
  public static double yawDegFromCameraTranslation(HeliosLocalizationVector t) {
    if (t == null) return Double.NaN;
    return Math.toDegrees(Math.atan2(t.x(), t.z()));
  }

  public static double pitchDegFromCameraTranslation(HeliosLocalizationVector t) {
    if (t == null) return Double.NaN;
    return Math.toDegrees(Math.atan2(t.y(), t.z()));
  }

  public static double distanceMetersFromCameraTranslation(HeliosLocalizationVector t) {
    if (t == null) return Double.NaN;
    return Math.sqrt(t.x() * t.x() + t.y() * t.y() + t.z() * t.z());
  }

  /** Pick the closest tag (by Euclidean distance in camera space). */
  public static HeliosLocalizationDetectionPose closestTag(List<HeliosLocalizationDetectionPose> tags) {
    if (tags == null || tags.isEmpty()) return null;
    HeliosLocalizationDetectionPose best = null;
    double bestD = Double.POSITIVE_INFINITY;
    for (HeliosLocalizationDetectionPose t : tags) {
      if (t == null || t.pose() == null) continue;
      double d = distanceMetersFromCameraTranslation(t.pose().translation());
      if (!Double.isFinite(d)) continue;
      if (d < bestD) {
        bestD = d;
        best = t;
      }
    }
    return best;
  }
}
