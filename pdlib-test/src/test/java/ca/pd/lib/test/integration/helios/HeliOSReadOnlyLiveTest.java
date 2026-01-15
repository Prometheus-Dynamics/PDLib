package ca.pd.lib.test.integration.helios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.pd.lib.helios.HeliOS;
import ca.pd.lib.helios.HeliOSOutput;
import ca.pd.lib.helios.HeliOSPipelineOnStream;
import ca.pd.lib.helios.HeliOSStream;
import ca.pd.lib.helios.localization.HeliosLocalizationProfile;
import ca.pd.lib.helios.model.HeliosPipelineSummary;
import ca.pd.lib.helios.model.HeliosStreamIdentity;
import ca.pd.lib.helios.model.HeliosStreamInfo;
import ca.pd.lib.helios.model.HeliosStreamControl;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/** Live read-only integration tests for the new HeliOS fluent API. */
public final class HeliOSReadOnlyLiveTest {
  @Test
  public void readOnlyFlowSmoke() throws Exception {
    boolean enabled = Boolean.parseBoolean(System.getProperty("helios.it", "false"));
    Assumptions.assumeTrue(enabled, "set -Dhelios.it=true or -PheliosIt=true to run live tests");

    String target =
        System.getProperty("helios.target", System.getenv().getOrDefault("HELIOS_TARGET", "172.31.250.1"));
    HeliOS cam = new HeliOS(target);

    // Device/peripheral sections.
    assertNotNull(cam.baseUri());
    assertNotNull(cam.device().health());
    assertNotNull(cam.peripherials().imu()); // spelling alias
    assertNotNull(cam.peripherals().fan());
    assertNotNull(cam.peripherals().leds());
    assertNotNull(cam.peripherals().power());

    // Localization and pipelines.
    List<HeliosLocalizationProfile> profiles = cam.localizations();
    assertNotNull(profiles);
    if (!profiles.isEmpty()) {
      assertNotNull(cam.localization(profiles.get(0).id()).solve());
    } else {
      assertNotNull(cam.localization().solve());
    }

    List<HeliosPipelineSummary> pipelines =
        cam.pipelines().stream().map(p -> p.summary()).toList();
    assertNotNull(pipelines);
    if (!pipelines.isEmpty()) {
      HeliosPipelineSummary first = pipelines.get(0);
      assertNotNull(cam.pipeline(first.id().toString()).graph());
      if (first.name() != null && !first.name().isBlank()) {
        assertEquals(first.id(), cam.pipeline(first.name()).id());
      }
    }

    // Stream flow.
    List<HeliOSStream> streams = cam.streams();
    assertNotNull(streams);
    Assumptions.assumeFalse(streams.isEmpty(), "no streams configured on target");

    HeliOSStream s0 = streams.get(0);
    HeliOSStream stream = cam.stream(s0.id().toString());
    HeliosStreamInfo info = stream.info();
    assertEquals(s0.id(), stream.id());
    assertNotNull(info);

    HeliosStreamIdentity identity = info.identity();
    if (identity.alias() != null && !identity.alias().isBlank()) {
      assertEquals(stream.id(), cam.stream(identity.alias()).id());
    } else if (identity.hardwareId() != null && !identity.hardwareId().isBlank()) {
      assertEquals(stream.id(), cam.stream(identity.hardwareId()).id());
    }

    assertNotNull(stream.settings());
    assertNotNull(stream.layout());
    assertNotNull(stream.pose());
    assertNotNull(stream.calibration());
    assertNotNull(stream.format());
    assertNotNull(stream.metrics());

    List<HeliosStreamControl> controls = stream.controls();
    assertNotNull(controls);
    if (!controls.isEmpty()) {
      HeliosStreamControl c0 = controls.get(0);
      assertEquals(c0.id(), stream.control(Long.toString(c0.id())).id());
      if (c0.name() != null && !c0.name().isBlank()) {
        assertEquals(c0.id(), stream.control(c0.name()).id());
      }
    }

    List<HeliOSOutput> outputs = stream.outputs();
    assertNotNull(outputs);
    if (!outputs.isEmpty()) {
      HeliOSOutput out0 = outputs.get(0);
      assertNotNull(out0.sample());
      assertNotNull(stream.output(out0.key()).sample());
    }

    List<HeliOSPipelineOnStream> onStream = stream.pipelines();
    assertNotNull(onStream);
    if (!onStream.isEmpty()) {
      HeliOSPipelineOnStream p0 = onStream.get(0);
      assertEquals(p0.id(), stream.pipeline(p0.id().toString()).id());
      if (p0.summary() != null && p0.summary().name() != null && !p0.summary().name().isBlank()) {
        assertEquals(p0.id(), stream.pipeline(p0.summary().name()).id());
      }
      if (p0.active()) {
        List<HeliOSOutput> pipelineOutputs = p0.outputs();
        assertNotNull(pipelineOutputs);
        if (!pipelineOutputs.isEmpty()) {
          assertNotNull(p0.output(pipelineOutputs.get(0).key()).sample());
        }
      }
    }
  }
}
