package ca.pd.lib.helios.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class HeliosStreamOutputsSocket implements AutoCloseable {
  public interface Listener {
    default void onOutputsList(List<String> outputs, long timestampMs) {}

    default void onSample(String port, JsonNode value, String error, long timestampMs) {}

    default void onAck(String requestId) {}

    default void onError(String requestId, String error) {}

    default void onClose(int statusCode, String reason) {}
  }

  private final HttpClient http;
  private final ObjectMapper mapper;
  private final URI wsUri;
  private final Listener listener;

  private volatile WebSocket socket;
  private volatile boolean closed = false;

  public HeliosStreamOutputsSocket(HttpClient http, ObjectMapper mapper, URI baseUri, UUID streamId, Listener listener) {
    this.http = Objects.requireNonNull(http, "http");
    this.mapper = Objects.requireNonNull(mapper, "mapper");
    this.listener = Objects.requireNonNull(listener, "listener");
    this.wsUri = HeliosWsUtil.toWebSocketUri(baseUri, "/v1/ws/streams/" + streamId + "/outputs");
  }

  public CompletableFuture<WebSocket> connectAndSubscribe(List<String> ports, int intervalMs) {
    if (closed) {
      return CompletableFuture.failedFuture(new IllegalStateException("already closed"));
    }
    CompletableFuture<WebSocket> fut =
        http.newWebSocketBuilder().buildAsync(wsUri, new WsListener());
    return fut.thenApply(
        ws -> {
          this.socket = ws;
          sendSubscribe(ports, intervalMs);
          return ws;
        });
  }

  public void sendSubscribe(List<String> ports, int intervalMs) {
    WebSocket ws = this.socket;
    if (ws == null || closed) {
      return;
    }

    ObjectNode req = mapper.createObjectNode();
    req.put("type", "subscribe");
    ArrayNode portsNode = req.putArray("ports");
    for (String port : ports) {
      String p = port == null ? "" : port.trim();
      if (!p.isEmpty()) {
        portsNode.add(p);
      }
    }
    if (intervalMs > 0) {
      req.put("interval_ms", intervalMs);
    }
    ws.sendText(req.toString(), true);
  }

  @Override
  public void close() {
    closed = true;
    WebSocket ws = socket;
    if (ws != null) {
      ws.sendClose(WebSocket.NORMAL_CLOSURE, "closed");
    }
  }

  private final class WsListener implements WebSocket.Listener {
    private final StringBuilder textBuffer = new StringBuilder();

    @Override
    public void onOpen(WebSocket webSocket) {
      webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
      textBuffer.append(data);
      if (!last) {
        webSocket.request(1);
        return CompletableFuture.completedFuture(null);
      }

      String text = textBuffer.toString();
      textBuffer.setLength(0);
      handleMessage(text);
      webSocket.request(1);
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
      webSocket.request(1);
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
      webSocket.request(1);
      return webSocket.sendPong(message);
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
      webSocket.request(1);
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
      listener.onClose(statusCode, reason);
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
      listener.onError(null, error == null ? "unknown websocket error" : error.getMessage());
    }

    private void handleMessage(String raw) {
      try {
        JsonNode node = mapper.readTree(raw);

        // StreamOutputsList: { outputs: [...], timestamp_ms: ... }
        if (node.has("outputs") && node.get("outputs").isArray()) {
          List<String> outputs = new ArrayList<>();
          for (JsonNode entry : node.get("outputs")) {
            String v = entry.asText("").trim();
            if (!v.isEmpty()) outputs.add(v);
          }
          long ts = node.path("timestamp_ms").asLong(0);
          listener.onOutputsList(outputs, ts);
          return;
        }

        // StreamOutputSampleEvent: { port, value?, error?, timestamp_ms }
        if (node.has("port")) {
          String port = node.path("port").asText("");
          JsonNode value = node.get("value");
          String err = node.has("error") && !node.get("error").isNull() ? node.get("error").asText(null) : null;
          long ts = node.path("timestamp_ms").asLong(0);
          listener.onSample(port, value, err, ts);
          return;
        }

        // Ack/Error: { type: "ack"|"error", request_id?, error? }
        if (node.has("type")) {
          String type = node.path("type").asText("");
          String requestId = node.path("request_id").isMissingNode() || node.path("request_id").isNull() ? null : node.path("request_id").asText(null);
          if ("ack".equalsIgnoreCase(type)) {
            listener.onAck(requestId);
            return;
          }
          if ("error".equalsIgnoreCase(type)) {
            listener.onError(requestId, node.path("error").asText("unknown error"));
            return;
          }
        }
      } catch (Exception e) {
        listener.onError(null, "failed to parse outputs message: " + e.getMessage());
      }
    }
  }
}
