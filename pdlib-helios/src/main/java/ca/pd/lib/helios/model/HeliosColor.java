package ca.pd.lib.helios.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** RGB(W) color payload used by the device lighting API. Channel values are 0-255. */
public record HeliosColor(int r, int g, int b, Integer w) {
  public ObjectNode toJson(ObjectMapper mapper) {
    ObjectNode n = mapper.createObjectNode();
    n.put("r", clamp8(r));
    n.put("g", clamp8(g));
    n.put("b", clamp8(b));
    if (w != null) n.put("w", clamp8(w));
    return n;
  }

  private static int clamp8(int v) {
    return Math.max(0, Math.min(255, v));
  }
}

