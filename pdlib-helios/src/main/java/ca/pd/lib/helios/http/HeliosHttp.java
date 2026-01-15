package ca.pd.lib.helios.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

/** Small HTTP helper for HeliOS' JSON API (typically served on {@code http://<ip>:5800}). */
public final class HeliosHttp {
  private final URI baseUri;
  private final HttpClient http;
  private final ObjectMapper mapper;
  private final Duration timeout;

  public HeliosHttp(URI baseUri, HttpClient http, ObjectMapper mapper, Duration timeout) {
    this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
    this.http = Objects.requireNonNull(http, "http");
    this.mapper = Objects.requireNonNull(mapper, "mapper");
    this.timeout = timeout == null ? Duration.ofSeconds(3) : timeout;
  }

  public URI baseUri() {
    return baseUri;
  }

  public ObjectMapper mapper() {
    return mapper;
  }

  public JsonNode getJson(String path) throws IOException, InterruptedException {
    URI uri = resolve(path);
    HttpRequest req =
        HttpRequest.newBuilder(uri)
            .timeout(timeout)
            .header("Accept", "application/json")
            .GET()
            .build();
    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
    if (res.statusCode() / 100 != 2) {
      throw new IOException("GET " + uri + " failed: " + res.statusCode() + " " + safeBody(res.body()));
    }
    return mapper.readTree(res.body());
  }

  public JsonNode postJson(String path, JsonNode body) throws IOException, InterruptedException {
    URI uri = resolve(path);
    HttpRequest req =
        HttpRequest.newBuilder(uri)
            .timeout(timeout)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body == null ? "null" : body.toString()))
            .build();
    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
    if (res.statusCode() / 100 != 2) {
      throw new IOException("POST " + uri + " failed: " + res.statusCode() + " " + safeBody(res.body()));
    }
    String raw = res.body() == null ? "" : res.body().trim();
    if (raw.isEmpty()) {
      return mapper.nullNode();
    }
    return mapper.readTree(raw);
  }

  public void postNoContent(String path, JsonNode body) throws IOException, InterruptedException {
    URI uri = resolve(path);
    HttpRequest req =
        HttpRequest.newBuilder(uri)
            .timeout(timeout)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body == null ? "null" : body.toString()))
            .build();
    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
    if (res.statusCode() != 204) {
      throw new IOException("POST " + uri + " failed: " + res.statusCode() + " " + safeBody(res.body()));
    }
  }

  public URI resolve(String path) {
    String p = path == null ? "" : path.trim();
    if (p.isEmpty()) {
      throw new IllegalArgumentException("path is required");
    }
    return baseUri.resolve(p.startsWith("/") ? p : "/" + p);
  }

  private static String safeBody(String body) {
    if (body == null) return "";
    String trimmed = body.trim();
    if (trimmed.length() <= 500) return trimmed;
    return trimmed.substring(0, 500) + "...";
  }
}

