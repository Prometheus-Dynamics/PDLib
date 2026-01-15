package ca.pd.lib.helios.examples.pipelines;

import ca.pd.lib.helios.HeliOS;
import ca.pd.lib.helios.HeliOSOutput;
import ca.pd.lib.helios.HeliOSStream;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;

/** Example: resolve a stream, list pipeline outputs, and sample a specific output port. */
public final class HeliosPipelineOutputsExample {
  private HeliosPipelineOutputsExample() {}

  public static void main(String[] args) throws Exception {
    String target = args.length > 0 ? args[0] : "172.31.250.1";
    String streamToken = args.length > 1 ? args[1] : "";
    String port = args.length > 2 ? args[2] : "detections";

    HeliOS cam = new HeliOS(target);
    HeliOSStream stream =
        streamToken == null || streamToken.isBlank() ? cam.streams().get(0) : cam.stream(streamToken);

    List<HeliOSOutput> outputs = stream.outputs();
    List<String> ports = new ArrayList<>(outputs.size());
    for (HeliOSOutput output : outputs) {
      ports.add(output.key());
    }

    System.out.println("stream_id=" + stream.id());
    System.out.println("ports=" + ports);

    if (!ports.contains(port)) {
      throw new IllegalStateException("stream has no output port: " + port);
    }

    JsonNode sample = stream.output(port).sample();
    System.out.println("sample_type=" + sample.getNodeType());
    System.out.println("sample=" + sample);
  }
}
