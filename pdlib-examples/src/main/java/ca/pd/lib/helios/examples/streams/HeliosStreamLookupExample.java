package ca.pd.lib.helios.examples.streams;

import ca.pd.lib.helios.HeliOS;
import ca.pd.lib.helios.HeliOSOutput;
import ca.pd.lib.helios.HeliOSStream;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/** Example: resolve stream by uuid/alias/hardware id, then sample a pipeline output port. */
public final class HeliosStreamLookupExample {
  private HeliosStreamLookupExample() {}

  public static JsonNode sampleDetections(HeliOS cam, String streamToken) throws Exception {
    HeliOSStream stream = cam.stream(streamToken);
    List<HeliOSOutput> outputs = stream.outputs();
    for (HeliOSOutput output : outputs) {
      if ("detections".equals(output.key())) {
        return output.sample();
      }
    }
    throw new IllegalStateException("stream has no 'detections' output port");
  }
}
