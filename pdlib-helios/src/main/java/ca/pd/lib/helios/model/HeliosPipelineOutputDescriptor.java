package ca.pd.lib.helios.model;

import com.fasterxml.jackson.databind.JsonNode;

/** Host output descriptor from {@code /v1/streams/{id}/pipeline/outputs}. */
public record HeliosPipelineOutputDescriptor(
    String name,
    JsonNode type,
    boolean previewable,
    JsonNode raw) {

  public static HeliosPipelineOutputDescriptor fromJson(JsonNode node) {
    if (node == null || node.isNull()) {
      throw new IllegalArgumentException("missing pipeline output descriptor");
    }
    String name = node.path("name").asText("").trim();
    if (name.isEmpty()) {
      throw new IllegalArgumentException("pipeline output descriptor missing name");
    }
    JsonNode type = node.path("ty");
    boolean previewable = node.path("previewable").asBoolean(false);
    return new HeliosPipelineOutputDescriptor(name, type, previewable, node);
  }
}
