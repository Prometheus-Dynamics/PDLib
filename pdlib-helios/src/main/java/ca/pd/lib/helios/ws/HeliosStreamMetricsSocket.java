package ca.pd.lib.helios.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class HeliosStreamMetricsSocket implements AutoCloseable {
  public interface Listener {
    default void onMetrics(UUID streamId, JsonNode metrics, long timestampMs) {}

    default void onError(String code, String error, String detail, long timestampMs) {}

    default void onClose(int statusCode, String reason) {}
  }

  private final HttpClient http;
  private final ObjectMapper mapper;
  private final URI wsUri;
  private final Listener listener;

  private volatile WebSocket socket;
  private volatile boolean closed = false;

  public HeliosStreamMetricsSocket(HttpClient http, ObjectMapper mapper, URI baseUri, UUID streamId, int intervalMs, Listener listener) {
    this.http = Objects.requireNonNull(http, "http");
    this.mapper = Objects.requireNonNull(mapper, "mapper");
    this.listener = Objects.requireNonNull(listener, "listener");
    String query = intervalMs > 0 ? ("?interval_ms=" + intervalMs) : "";
    this.wsUri = HeliosWsUtil.toWebSocketUri(baseUri, "/v1/ws/streams/" + streamId + "/metrics" + query);
  }

  public CompletableFuture<WebSocket> connect() {
    if (closed) {
      return CompletableFuture.failedFuture(new IllegalStateException("already closed"));
    }
    CompletableFuture<WebSocket> fut = http.newWebSocketBuilder().buildAsync(wsUri, new WsListener());
    return fut.thenApply(
        ws -> {
          this.socket = ws;
          return ws;
        });
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
      listener.onError("ws_error", error == null ? "unknown websocket error" : error.getMessage(), null, 0);
    }

    private void handleMessage(String raw) {
      try {
        JsonNode node = mapper.readTree(raw);

        // StreamMetricsEvent: { stream_id, metrics, timestamp_ms }
        if (node.has("metrics")) {
          UUID streamId = UUID.fromString(node.path("stream_id").asText());
          JsonNode metrics = node.get("metrics");
          long ts = node.path("timestamp_ms").asLong(0);
          listener.onMetrics(streamId, metrics, ts);
          return;
        }

        // StreamMetricsError: { error, code?, detail?, timestamp_ms }
        if (node.has("error")) {
          String code = node.has("code") && !node.get("code").isNull() ? node.get("code").asText(null) : null;
          String error = node.path("error").asText("metrics error");
          String detail = node.has("detail") && !node.get("detail").isNull() ? node.get("detail").asText(null) : null;
          long ts = node.path("timestamp_ms").asLong(0);
          listener.onError(code, error, detail, ts);
        }
      } catch (Exception e) {
        listener.onError("parse_error", "failed to parse metrics message: " + e.getMessage(), raw, 0);
      }
    }
  }
}
