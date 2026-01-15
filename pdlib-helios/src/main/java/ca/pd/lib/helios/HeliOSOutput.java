package ca.pd.lib.helios;

import ca.pd.lib.helios.model.HeliosPipelineOutputDescriptor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/** Stream pipeline output port reference. */
public final class HeliOSOutput {
  private final HeliOS helios;
  private final UUID streamId;
  private final HeliosPipelineOutputDescriptor descriptor;

  HeliOSOutput(HeliOS helios, UUID streamId, HeliosPipelineOutputDescriptor descriptor) {
    this.helios = Objects.requireNonNull(helios, "helios");
    this.streamId = Objects.requireNonNull(streamId, "streamId");
    this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
  }

  public String key() {
    return descriptor.name();
  }

  public boolean previewable() {
    return descriptor.previewable();
  }

  public JsonNode type() {
    return descriptor.type();
  }

  public HeliosPipelineOutputDescriptor descriptor() {
    return descriptor;
  }

  public JsonNode sample() throws Exception {
    try {
      return helios.streamsClient().samplePipelineOutput(streamId, descriptor.name());
    } catch (IOException ex) {
      if (isSampleUnavailable(ex)) {
        return MissingNode.getInstance();
      }
      throw ex;
    }
  }

  private static boolean isSampleUnavailable(IOException ex) {
    String msg = ex.getMessage();
    if (msg == null) return false;
    return msg.contains(" failed: 404")
        || msg.contains("engine_code\":\"NotFound\"")
        || msg.toLowerCase().contains("sample unavailable");
  }
}
