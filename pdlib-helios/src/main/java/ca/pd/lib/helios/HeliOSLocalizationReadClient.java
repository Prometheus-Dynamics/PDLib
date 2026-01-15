package ca.pd.lib.helios;

import ca.pd.lib.helios.http.HeliosHttp;
import ca.pd.lib.helios.localization.HeliosLatencyMetrics;
import ca.pd.lib.helios.localization.HeliosLocalizationConfig;
import ca.pd.lib.helios.localization.HeliosLocalizationDetectionPose;
import ca.pd.lib.helios.localization.HeliosLocalizationSolveResponse;
import ca.pd.lib.helios.localization.HeliosLocalizationSolverOutputs;
import ca.pd.lib.helios.localization.HeliosLocalizationSolverPose;
import ca.pd.lib.helios.localization.HeliosLocalizationSolverResult;
import ca.pd.lib.helios.localization.HeliosLocalizationSourcePose;
import ca.pd.lib.helios.localization.HeliosLocalizationSourceSampleStatus;
import ca.pd.lib.helios.localization.HeliosLocalizationUtil;
import ca.pd.lib.helios.localization.HeliosPoseEstimate2d;
import ca.pd.lib.helios.localization.HeliosPoseSpace;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj.Timer;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class HeliOSLocalizationReadClient {
  private final HeliosHttp http;
  private final ObjectMapper mapper;

  HeliOSLocalizationReadClient(HeliosHttp http) {
    this.http = Objects.requireNonNull(http, "http");
    this.mapper = http.mapper();
  }

  HeliosLocalizationConfig getConfig() throws IOException, InterruptedException {
    JsonNode node = http.getJson("/v1/localization/config");
    return mapper.treeToValue(node, HeliosLocalizationConfig.class);
  }

  HeliosLocalizationSolveResponse solve(String profileId) throws IOException, InterruptedException {
    String query = "";
    if (profileId != null && !profileId.trim().isEmpty()) {
      query = "?profile_id=" + URLEncoder.encode(profileId.trim(), StandardCharsets.UTF_8);
    }
    JsonNode node = http.getJson("/v1/localization/solve" + query);
    return mapper.treeToValue(node, HeliosLocalizationSolveResponse.class);
  }

  HeliosLocalizationSolveResponse solve() throws IOException, InterruptedException {
    return solve(null);
  }

  Optional<HeliosPoseEstimate2d> getRobotInFieldEstimate(String profileId) throws IOException, InterruptedException {
    long t0 = System.nanoTime();
    HeliosLocalizationSolveResponse res = solve(profileId);
    long t1 = System.nanoTime();

    HeliosLocalizationSolverResult solver = null;
    for (HeliosLocalizationSolverResult s : res.solvers()) {
      if (s != null && s.outputs() != null && s.outputs().robotInField() != null) {
        solver = s;
        break;
      }
    }
    if (solver == null) return Optional.empty();

    HeliosLocalizationSolverPose robot = solver.outputs().robotInField();
    if (robot == null || robot.pose() == null) return Optional.empty();

    Pose2d pose2d = HeliosLocalizationUtil.toWpilibPose2dField(robot.pose());

    List<HeliosLocalizationSourceSampleStatus> sources = res.sources() == null ? List.of() : res.sources();
    int srcCount = sources.size();
    double maxPoll = 0.0;
    double sumPoll = 0.0;
    int dets = 0;
    for (HeliosLocalizationSourceSampleStatus status : sources) {
      if (status == null) continue;
      double poll = status.pollMs();
      if (Double.isFinite(poll)) {
        maxPoll = Math.max(maxPoll, poll);
        sumPoll += poll;
      }
      dets += Math.max(0, status.detections());
    }
    double avgPoll = srcCount > 0 ? (sumPoll / srcCount) : 0.0;
    double httpWallMs = (t1 - t0) / 1e6;
    HeliosLatencyMetrics latency = new HeliosLatencyMetrics(httpWallMs, maxPoll, avgPoll, srcCount, dets);

    List<HeliosLocalizationDetectionPose> tagInCamera = solver.outputs().tagInCamera();
    int tagCount = tagInCamera == null ? 0 : tagInCamera.size();
    double avgDist = HeliosLocalizationUtil.averageTagDistanceMeters(tagInCamera);
    Matrix<N3, N1> stdDevs = HeliosLocalizationUtil.estimateFieldPoseStdDevs(tagCount, avgDist);

    double estLatencySec = Math.max(0.0, (httpWallMs + maxPoll) / 1000.0);
    double timestampSec = Timer.getFPGATimestamp() - estLatencySec;

    return Optional.of(new HeliosPoseEstimate2d(pose2d, timestampSec, stdDevs, latency));
  }

  Optional<HeliosLocalizationSolverResult> pickSolver(HeliosLocalizationSolveResponse res, String solverId) {
    if (res == null || res.solvers() == null || res.solvers().isEmpty()) return Optional.empty();
    if (solverId == null || solverId.isBlank()) {
      return Optional.ofNullable(res.solvers().get(0));
    }
    for (HeliosLocalizationSolverResult s : res.solvers()) {
      if (s != null && solverId.equals(s.id())) return Optional.of(s);
    }
    return Optional.empty();
  }

  List<HeliosLocalizationDetectionPose> detectionsInSpace(
      HeliosLocalizationSolveResponse res, HeliosPoseSpace space, String solverId) {
    HeliosLocalizationSolverResult solver = pickSolver(res, solverId).orElse(null);
    if (solver == null || solver.outputs() == null || space == null) return List.of();

    HeliosLocalizationSolverOutputs outputs = solver.outputs();
    return switch (space) {
      case tag_in_camera -> outputs.tagInCamera() == null ? List.of() : outputs.tagInCamera();
      case camera_in_tag -> outputs.cameraInTag() == null ? List.of() : outputs.cameraInTag();
      case tag_in_robot -> outputs.tagInRobot() == null ? List.of() : outputs.tagInRobot();
      case robot_in_tag -> outputs.robotInTag() == null ? List.of() : outputs.robotInTag();
      default -> List.of();
    };
  }

  List<HeliosLocalizationSourcePose> cameraInField(HeliosLocalizationSolveResponse res, String solverId) {
    HeliosLocalizationSolverResult solver = pickSolver(res, solverId).orElse(null);
    if (solver == null || solver.outputs() == null) return List.of();
    List<HeliosLocalizationSourcePose> cameraInField = solver.outputs().cameraInField();
    return cameraInField == null ? List.of() : cameraInField;
  }

  Optional<HeliosLocalizationSolverPose> robotInField(HeliosLocalizationSolveResponse res, String solverId) {
    HeliosLocalizationSolverResult solver = pickSolver(res, solverId).orElse(null);
    if (solver == null || solver.outputs() == null) return Optional.empty();
    return Optional.ofNullable(solver.outputs().robotInField());
  }
}
