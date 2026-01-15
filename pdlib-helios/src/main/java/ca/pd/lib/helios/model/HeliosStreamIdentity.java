package ca.pd.lib.helios.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Identity fields used to look up a stream (uuid, alias, hardware id). */
public record HeliosStreamIdentity(UUID id, String alias, String hardwareId) {
  public static HeliosStreamIdentity fromJson(JsonNode identityNode) {
    if (identityNode == null || identityNode.isNull()) {
      throw new IllegalArgumentException("missing identity");
    }
    String idRaw = identityNode.path("id").asText("").trim();
    if (idRaw.isEmpty()) {
      throw new IllegalArgumentException("identity.id is required");
    }
    UUID id = UUID.fromString(idRaw);

    String alias = null;
    if (identityNode.has("alias") && !identityNode.get("alias").isNull()) {
      String v = identityNode.get("alias").asText("").trim();
      if (!v.isEmpty()) alias = v;
    }

    String hardwareId = null;
    if (identityNode.has("hardware_id") && !identityNode.get("hardware_id").isNull()) {
      String v = identityNode.get("hardware_id").asText("").trim();
      if (!v.isEmpty()) hardwareId = v;
    }

    return new HeliosStreamIdentity(id, alias, hardwareId);
  }

  /**
   * Tokens used to look up a stream (id string + alias + hardware id).
   *
   * <p>HeliOS backend is responsible for enforcing identity uniqueness.
   */
  public List<String> tokens() {
    List<String> out = new ArrayList<>(3);
    out.add(id.toString());
    if (alias != null && !alias.isBlank()) out.add(alias.trim());
    if (hardwareId != null && !hardwareId.isBlank()) out.add(hardwareId.trim());
    return out;
  }
}
