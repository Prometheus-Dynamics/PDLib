package ca.pd.lib.helios.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** High-signal stream information used by robot code. */
public record HeliosStreamInfo(
    UUID id,
    HeliosStreamIdentity identity,
    boolean internal,
    UUID activePipelineId,
    String activePipelineOutput,
    List<HeliosStreamPipelineBinding> pipelines,
    JsonNode raw) {

  public static HeliosStreamInfo fromJson(JsonNode node) {
    if (node == null || node.isNull()) {
      throw new IllegalArgumentException("missing stream");
    }
    String idRaw = node.path("id").asText("").trim();
    if (idRaw.isEmpty()) {
      throw new IllegalArgumentException("stream missing id");
    }
    UUID id = UUID.fromString(idRaw);
    JsonNode manifest = node.path("manifest");
    HeliosStreamIdentity ident = HeliosStreamIdentity.fromJson(manifest.path("identity"));
    boolean internal = manifest.path("internal").asBoolean(false);

    UUID activePipelineId = null;
    if (manifest.has("active_pipeline_id") && !manifest.get("active_pipeline_id").isNull()) {
      String rawId = manifest.get("active_pipeline_id").asText("").trim();
      if (!rawId.isEmpty()) activePipelineId = UUID.fromString(rawId);
    } else if (manifest.has("pipeline_id") && !manifest.get("pipeline_id").isNull()) {
      String rawId = manifest.get("pipeline_id").asText("").trim();
      if (!rawId.isEmpty()) activePipelineId = UUID.fromString(rawId);
    }

    String activePipelineOutput = null;
    if (manifest.has("active_pipeline_output") && !manifest.get("active_pipeline_output").isNull()) {
      String v = manifest.get("active_pipeline_output").asText("").trim();
      if (!v.isEmpty()) activePipelineOutput = v;
    } else if (manifest.has("pipeline_output") && !manifest.get("pipeline_output").isNull()) {
      String v = manifest.get("pipeline_output").asText("").trim();
      if (!v.isEmpty()) activePipelineOutput = v;
    }

    List<HeliosStreamPipelineBinding> pipelines = new ArrayList<>();
    JsonNode bindings = manifest.get("pipelines");
    if (bindings instanceof ArrayNode arr) {
      for (JsonNode b : arr) {
        pipelines.add(HeliosStreamPipelineBinding.fromJson(b));
      }
    }

    return new HeliosStreamInfo(id, ident, internal, activePipelineId, activePipelineOutput, List.copyOf(pipelines), node);
  }
}
