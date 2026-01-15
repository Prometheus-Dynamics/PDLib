package ca.pd.lib.helios.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;

/** Stream control metadata/value from {@code /v1/streams/{id}/controls}. */
public record HeliosStreamControl(
    long id,
    String name,
    String kind,
    JsonNode value,
    JsonNode raw) {

  public static HeliosStreamControl fromJson(JsonNode node) {
    if (node == null || node.isNull()) {
      throw new IllegalArgumentException("missing stream control");
    }
    long id = node.path("id").asLong(-1);
    if (id < 0) {
      throw new IllegalArgumentException("stream control missing id");
    }
    String name = node.path("name").asText("").trim();
    String kind = node.path("kind").asText("").trim();
    JsonNode value = node.path("value");
    return new HeliosStreamControl(id, name, kind, value, node);
  }

  public boolean matchesToken(String token) {
    String key = normalizeLookupToken(token).orElse(null);
    if (key == null) {
      return false;
    }
    if (Long.toString(id).equals(key)) {
      return true;
    }
    String n = normalizeLookupToken(name).orElse(null);
    return n != null && n.equals(key);
  }

  private static Optional<String> normalizeLookupToken(String token) {
    if (token == null) return Optional.empty();
    String t = token.trim();
    if (t.isEmpty()) return Optional.empty();
    return Optional.of(t.toLowerCase());
  }
}
