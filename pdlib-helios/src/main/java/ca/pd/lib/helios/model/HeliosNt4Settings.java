package ca.pd.lib.helios.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Device NT4 settings (what host/port HeliOS publishes to). */
public record HeliosNt4Settings(
    boolean enabled,
    boolean subscriptionsEnabled,
    String serverHost,
    int serverPort,
    String publicApiUrl) {

  public static HeliosNt4Settings fromJson(JsonNode node) {
    if (node == null || node.isNull()) {
      throw new IllegalArgumentException("missing nt4 settings");
    }
    boolean enabled = node.path("enabled").asBoolean(false);
    boolean subs = node.path("subscriptions_enabled").asBoolean(false);
    String host = node.has("server_host") && !node.get("server_host").isNull() ? node.get("server_host").asText(null) : null;
    int port = node.path("server_port").asInt(5810);
    String apiUrl = node.has("public_api_url") && !node.get("public_api_url").isNull() ? node.get("public_api_url").asText(null) : null;
    return new HeliosNt4Settings(enabled, subs, host, port, apiUrl);
  }

  public ObjectNode toJson(ObjectMapper mapper) {
    ObjectNode n = mapper.createObjectNode();
    n.put("enabled", enabled);
    n.put("subscriptions_enabled", subscriptionsEnabled);
    if (serverHost == null) {
      n.putNull("server_host");
    } else {
      String trimmed = serverHost.trim();
      if (trimmed.isEmpty()) {
        n.putNull("server_host");
      } else {
        n.put("server_host", trimmed);
      }
    }
    n.put("server_port", serverPort);
    if (publicApiUrl == null) {
      n.putNull("public_api_url");
    } else {
      String trimmed = publicApiUrl.trim();
      if (trimmed.isEmpty()) n.putNull("public_api_url");
      else n.put("public_api_url", trimmed);
    }
    return n;
  }
}

