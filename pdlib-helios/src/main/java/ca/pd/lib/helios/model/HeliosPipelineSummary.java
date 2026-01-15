package ca.pd.lib.helios.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Summary for a stored pipeline graph. */
public record HeliosPipelineSummary(UUID id, String name, long updatedAtMs, int issueCount, JsonNode raw) {
  public static HeliosPipelineSummary fromJson(JsonNode node) {
    if (node == null || node.isNull()) throw new IllegalArgumentException("missing pipeline summary");
    String idRaw = node.path("id").asText("").trim();
    if (idRaw.isEmpty()) throw new IllegalArgumentException("pipeline summary missing id");
    UUID id = UUID.fromString(idRaw);

    String name = null;
    if (node.has("name") && !node.get("name").isNull()) {
      String v = node.get("name").asText("").trim();
      if (!v.isEmpty()) name = v;
    }
    long updatedAtMs = node.path("updated_at_ms").asLong(0);
    int issueCount = node.path("issue_count").asInt(0);
    return new HeliosPipelineSummary(id, name, updatedAtMs, issueCount, node);
  }

  /**
   * Tokens used to look up a pipeline (id string + name).
   *
   * <p>HeliOS backend is responsible for enforcing identity uniqueness.
   */
  public List<String> tokens() {
    List<String> out = new ArrayList<>(2);
    out.add(id.toString());
    if (name != null && !name.isBlank()) out.add(name.trim());
    return out;
  }
}
