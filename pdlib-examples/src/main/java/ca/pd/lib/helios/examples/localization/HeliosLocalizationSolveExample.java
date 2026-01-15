package ca.pd.lib.helios.examples.localization;

import ca.pd.lib.core.json.JsonSupport;
import ca.pd.lib.helios.HeliOS;
import ca.pd.lib.helios.localization.HeliosLocalizationSolveResponse;
import ca.pd.lib.helios.localization.HeliosPoseEstimate2d;
import ca.pd.lib.helios.vision.HeliosVisionHelpers;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Example: call /v1/localization/solve and work with pose outputs + latency/std-dev estimate. */
public final class HeliosLocalizationSolveExample {
  private HeliosLocalizationSolveExample() {}

  public static void main(String[] args) throws Exception {
    String target = args.length > 0 ? args[0] : "172.31.250.1";
    String profileId = args.length > 1 ? args[1] : null;
    String solverId = args.length > 2 ? args[2] : null;

    HeliOS cam = new HeliOS(target);
    ObjectMapper mapper = JsonSupport.MAPPER;

    HeliosLocalizationSolveResponse solve =
        (profileId == null || profileId.isBlank())
            ? cam.localization().solve()
            : cam.localization(profileId).solve();
    System.out.println("solve=" + mapper.writeValueAsString(solve));

    var tagInCamera = HeliosVisionHelpers.tagInCamera(solve, solverId);
    System.out.println("tag_in_camera_count=" + tagInCamera.size());

    HeliosPoseEstimate2d estimate =
        (profileId == null || profileId.isBlank())
            ? cam.localization().fieldPoseBlueEstimate().orElse(null)
            : cam.localization(profileId).fieldPoseBlueEstimate().orElse(null);
    if (estimate == null) {
      System.out.println("robot_in_field=<none>");
      return;
    }

    System.out.println("robot_in_field_pose=" + estimate.pose());
    System.out.println("timestamp_sec=" + estimate.timestampSeconds());
    System.out.println("std_devs=" + estimate.stdDevs());
    System.out.println("latency=" + estimate.latency());
  }
}
