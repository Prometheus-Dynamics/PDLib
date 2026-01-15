package ca.pd.lib.helios.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.pd.lib.helios.HeliOS;
import ca.pd.lib.helios.HeliOSOutput;
import ca.pd.lib.helios.HeliOSStream;
import ca.pd.lib.helios.model.HeliosStreamControl;
import ca.pd.lib.helios.model.HeliosStreamInfo;
import ca.pd.lib.helios.vision.HeliosVisionHelpers;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/** Optional smoke test against a live HeliOS device using the read-only API. */
public final class HeliosLiveDeviceIntegrationTest {
  @Test
  public void liveDeviceSmoke() throws Exception {
    boolean enabled = Boolean.parseBoolean(System.getProperty("helios.it", "false"));
    Assumptions.assumeTrue(enabled, "set -Dhelios.it=true to enable live-device tests");

    String target =
        System.getProperty(
            "helios.target",
            System.getProperty("helios.baseUrl", System.getenv().getOrDefault("HELIOS_TARGET", "172.31.250.1")));
    HeliOS cam = new HeliOS(target);

    List<HeliOSStream> streams = cam.streams();
    assertFalse(streams.isEmpty(), "expected at least one stream");
    HeliOSStream s0 = streams.get(0);

    assertNotNull(cam.stream(s0.id().toString()));
    HeliosStreamInfo info = s0.info();
    if (info.identity().hardwareId() != null && !info.identity().hardwareId().isBlank()) {
      assertEquals(s0.id(), cam.stream(info.identity().hardwareId()).id());
    }

    List<HeliOSOutput> outputs = s0.outputs();
    assertFalse(outputs.isEmpty(), "expected at least one output port");
    for (HeliOSOutput output : outputs) {
      if ("detections".equals(output.key())) {
        var sample = output.sample();
        var detections = HeliosVisionHelpers.parseFiducialDetections2d(sample);
        assertNotNull(detections);
        break;
      }
    }

    assertNotNull(cam.device().metrics());
    assertNotNull(cam.peripherals().fan());
    assertNotNull(cam.peripherals().leds());
    assertNotNull(cam.device().nt4());

    assertNotNull(cam.localizations());
    assertNotNull(cam.localization().solve());

    List<HeliosStreamControl> controls = s0.controls();
    assertNotNull(controls);
    if (!controls.isEmpty()) {
      HeliosStreamControl c0 = controls.get(0);
      assertEquals(c0.id(), s0.control(Long.toString(c0.id())).id());
    }
  }
}
