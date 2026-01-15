package ca.pd.lib.helios.examples.pipelines;

import ca.pd.lib.helios.HeliOS;
import ca.pd.lib.helios.HeliOSPipelineOnStream;
import ca.pd.lib.helios.HeliOSStream;

/** Example: resolve a stream, then fetch the active pipeline it is currently using. */
public final class HeliosStreamPipelineExample {
  private HeliosStreamPipelineExample() {}

  public static void main(String[] args) throws Exception {
    String target = args.length > 0 ? args[0] : "172.31.250.1";
    String streamToken = args.length > 1 ? args[1] : "";

    HeliOS cam = new HeliOS(target);
    HeliOSStream stream =
        streamToken == null || streamToken.isBlank() ? cam.streams().get(0) : cam.stream(streamToken);

    HeliOSPipelineOnStream pipeline = stream.pipeline();
    System.out.println("stream_id=" + stream.id() + " pipeline_id=" + pipeline.id() + " name=" + pipeline.name());
    System.out.println("pipeline_graph=" + pipeline.graph());
  }
}
