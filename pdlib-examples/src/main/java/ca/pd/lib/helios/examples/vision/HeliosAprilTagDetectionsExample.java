package ca.pd.lib.helios.examples.vision;

import ca.pd.lib.helios.HeliOS;
import ca.pd.lib.helios.HeliOSStream;
import ca.pd.lib.helios.localization.HeliosLocalizationSolveResponse;
import ca.pd.lib.helios.vision.HeliosFiducialDetection2d;
import ca.pd.lib.helios.vision.HeliosVisionHelpers;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Example: sample a pipeline detections output and parse AprilTag-style detections, plus a few
 * "Limelight/PhotonVision style" helpers (yaw/pitch/distance) using localization tagInCamera.
 */
public final class HeliosAprilTagDetectionsExample {
  private HeliosAprilTagDetectionsExample() {}

  public static void main(String[] args) throws Exception {
    String target = args.length > 0 ? args[0] : "172.31.250.1";
    String streamToken = args.length > 1 ? args[1] : "";
    String detectionsPort = args.length > 2 ? args[2] : "detections";
    String profileId = args.length > 3 ? args[3] : null;

    HeliOS cam = new HeliOS(target);
    HeliOSStream stream =
        streamToken == null || streamToken.isBlank() ? cam.streams().get(0) : cam.stream(streamToken);

    JsonNode sample = stream.output(detectionsPort).sample();
    List<HeliosFiducialDetection2d> dets = HeliosVisionHelpers.parseFiducialDetections2d(sample);
    System.out.println("detections_2d_count=" + dets.size());
    if (!dets.isEmpty()) {
      System.out.println("first_det_id=" + dets.get(0).id() + " corners=" + dets.get(0).corners().size());
    }

    HeliosLocalizationSolveResponse solve =
        (profileId == null || profileId.isBlank())
            ? cam.localization().solve()
            : cam.localization(profileId).solve();
    var tagInCamera = HeliosVisionHelpers.tagInCamera(solve, null);
    var closest = HeliosVisionHelpers.closestTag(tagInCamera);
    if (closest == null || closest.pose() == null) {
      System.out.println("closest_tag=<none>");
      return;
    }

    var translation = closest.pose().translation();
    System.out.println("closest_tag_id=" + closest.tagId());
    System.out.println("yaw_deg=" + HeliosVisionHelpers.yawDegFromCameraTranslation(translation));
    System.out.println("pitch_deg=" + HeliosVisionHelpers.pitchDegFromCameraTranslation(translation));
    System.out.println("distance_m=" + HeliosVisionHelpers.distanceMetersFromCameraTranslation(translation));
  }
}
