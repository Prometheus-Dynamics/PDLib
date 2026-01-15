package ca.pd.lib.helios.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

/** A pipeline attached to a stream (pipeline uuid + optional selected output port). */
public record HeliosStreamPipelineBinding(UUID pipelineId, String pipelineOutput) {
  public static HeliosStreamPipelineBinding fromJson(JsonNode node) {
    if (node == null || node.isNull()) {
      throw new IllegalArgumentException("missing pipeline binding");
    }
    String idRaw = node.path("pipeline_id").asText("").trim();
    if (idRaw.isEmpty()) {
      throw new IllegalArgumentException("pipeline binding missing pipeline_id");
    }
    UUID pipelineId = UUID.fromString(idRaw);

    String out = null;
    if (node.has("pipeline_output") && !node.get("pipeline_output").isNull()) {
      String v = node.get("pipeline_output").asText("").trim();
      if (!v.isEmpty()) out = v;
    }
    return new HeliosStreamPipelineBinding(pipelineId, out);
  }
}

