package ca.pd.lib.helios;

import ca.pd.lib.helios.http.HeliosHttp;
import ca.pd.lib.helios.model.HeliosPipelineOutputDescriptor;
import ca.pd.lib.helios.model.HeliosStreamControl;
import ca.pd.lib.helios.model.HeliosStreamIdentity;
import ca.pd.lib.helios.model.HeliosStreamInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

final class HeliOSStreamsReadClient {
  private final HeliosHttp http;

  HeliOSStreamsReadClient(HeliosHttp http) {
    this.http = Objects.requireNonNull(http, "http");
  }

  List<HeliosStreamInfo> listStreams(boolean includeInternal) throws IOException, InterruptedException {
    JsonNode node = http.getJson("/v1/streams");
    if (!(node instanceof ArrayNode arr)) {
      throw new IOException("unexpected /v1/streams response shape (expected array)");
    }
    List<HeliosStreamInfo> out = new ArrayList<>(arr.size());
    for (JsonNode entry : arr) {
      HeliosStreamInfo info = HeliosStreamInfo.fromJson(entry);
      if (!includeInternal && info.internal()) {
        continue;
      }
      out.add(info);
    }
    return List.copyOf(out);
  }

  List<HeliosStreamInfo> listStreams() throws IOException, InterruptedException {
    return listStreams(false);
  }

  HeliosStreamInfo getStream(UUID id) throws IOException, InterruptedException {
    Objects.requireNonNull(id, "id");
    JsonNode node = http.getJson("/v1/streams/" + id);
    return HeliosStreamInfo.fromJson(node);
  }

  HeliosStreamInfo resolveStream(String token) throws IOException, InterruptedException {
    String key = normalizeLookupToken(token);

    List<HeliosStreamInfo> streams = listStreams(false);
    List<HeliosStreamInfo> matches = new ArrayList<>();
    for (HeliosStreamInfo stream : streams) {
      HeliosStreamIdentity ident = stream.identity();
      for (String raw : ident.tokens()) {
        if (normalizeLookupToken(raw).equals(key)) {
          matches.add(stream);
          break;
        }
      }
    }

    if (matches.isEmpty()) {
      throw new IllegalArgumentException("stream not found for token: " + token);
    }
    if (matches.size() > 1) {
      String ids =
          matches.stream()
              .map(stream -> stream.id().toString())
              .distinct()
              .limit(5)
              .reduce((a, b) -> a + ", " + b)
              .orElse("");
      throw new IllegalStateException(
          "ambiguous stream token (matches "
              + matches.size()
              + " streams): token="
              + token
              + " ids=["
              + ids
              + "]");
    }
    return matches.get(0);
  }

  Optional<HeliosStreamInfo> findStreamByAlias(String alias) throws IOException, InterruptedException {
    if (alias == null || alias.trim().isEmpty()) return Optional.empty();
    String key = alias.trim();
    for (HeliosStreamInfo stream : listStreams(false)) {
      if (stream.identity().alias() != null && stream.identity().alias().trim().equalsIgnoreCase(key)) {
        return Optional.of(stream);
      }
    }
    return Optional.empty();
  }

  List<String> listPipelineOutputs(UUID streamId) throws IOException, InterruptedException {
    List<HeliosPipelineOutputDescriptor> descriptors = listPipelineOutputDescriptors(streamId);
    List<String> out = new ArrayList<>(descriptors.size());
    for (HeliosPipelineOutputDescriptor descriptor : descriptors) {
      out.add(descriptor.name());
    }
    return List.copyOf(out);
  }

  List<HeliosPipelineOutputDescriptor> listPipelineOutputDescriptors(UUID streamId) throws IOException, InterruptedException {
    Objects.requireNonNull(streamId, "streamId");
    JsonNode node = http.getJson("/v1/streams/" + streamId + "/pipeline/outputs");
    if (!(node instanceof ArrayNode arr)) {
      throw new IOException("unexpected pipeline outputs response shape (expected array)");
    }
    List<HeliosPipelineOutputDescriptor> out = new ArrayList<>(arr.size());
    for (JsonNode entry : arr) {
      out.add(HeliosPipelineOutputDescriptor.fromJson(entry));
    }
    return List.copyOf(out);
  }

  JsonNode samplePipelineOutput(UUID streamId, String port) throws IOException, InterruptedException {
    Objects.requireNonNull(streamId, "streamId");
    String encoded = URLEncoder.encode(normalizeToken(port), StandardCharsets.UTF_8);
    return http.getJson("/v1/streams/" + streamId + "/pipeline/outputs/" + encoded + "/sample");
  }

  JsonNode getStreamMetrics(UUID streamId) throws IOException, InterruptedException {
    Objects.requireNonNull(streamId, "streamId");
    return http.getJson("/v1/streams/" + streamId + "/metrics");
  }

  JsonNode getStreamFormat(UUID streamId) throws IOException, InterruptedException {
    Objects.requireNonNull(streamId, "streamId");
    return http.getJson("/v1/streams/" + streamId + "/format");
  }

  List<HeliosStreamControl> listControls(UUID streamId) throws IOException, InterruptedException {
    Objects.requireNonNull(streamId, "streamId");
    JsonNode node = http.getJson("/v1/streams/" + streamId + "/controls");
    if (!(node instanceof ArrayNode arr)) {
      throw new IOException("unexpected controls response shape (expected array)");
    }
    List<HeliosStreamControl> out = new ArrayList<>(arr.size());
    for (JsonNode entry : arr) {
      out.add(HeliosStreamControl.fromJson(entry));
    }
    return List.copyOf(out);
  }

  HeliosStreamControl resolveControl(UUID streamId, String token) throws IOException, InterruptedException {
    normalizeToken(token);
    List<HeliosStreamControl> controls = listControls(streamId);
    List<HeliosStreamControl> matches = new ArrayList<>();
    for (HeliosStreamControl control : controls) {
      if (control.matchesToken(token)) {
        matches.add(control);
      }
    }
    if (matches.isEmpty()) {
      throw new IllegalArgumentException("control not found for token: " + token);
    }
    if (matches.size() > 1) {
      String ids =
          matches.stream()
              .map(control -> Long.toString(control.id()))
              .distinct()
              .limit(5)
              .reduce((a, b) -> a + ", " + b)
              .orElse("");
      throw new IllegalStateException(
          "ambiguous control token (matches "
              + matches.size()
              + " controls): token="
              + token
              + " ids=["
              + ids
              + "]");
    }
    return matches.get(0);
  }

  private static String normalizeToken(String token) {
    String out = token == null ? "" : token.trim();
    if (out.isEmpty()) throw new IllegalArgumentException("token is required");
    return out;
  }

  private static String normalizeLookupToken(String token) {
    String out = normalizeToken(token);
    return tryNormalizeUuidToken(out).orElseGet(() -> out.toLowerCase());
  }

  private static Optional<String> tryNormalizeUuidToken(String token) {
    try {
      return Optional.of(UUID.fromString(token).toString());
    } catch (IllegalArgumentException ignored) {
      return Optional.empty();
    }
  }
}
