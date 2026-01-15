package ca.pd.lib.helios.nt4;

import ca.pd.lib.core.json.JsonSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringSubscriber;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Reads the NT4 topics published by HeliOS' NT4 bridge.
 *
 * Expected topics (prefix is typically the device hostname, e.g. "/helios-front"):
 * - {@code <prefix>/info/hostname} (string)
 * - {@code <prefix>/info/ip} (string, optional)
 * - {@code <prefix>/info/api_url} (string)
 * - {@code <prefix>/info/streams} (json string array/object)
 * - {@code <prefix>/telemetry} (json string, optional)
 */
public final class HeliosNt4Device implements AutoCloseable {
  private static final ObjectMapper MAPPER = JsonSupport.MAPPER;

  private final NetworkTableInstance inst;
  private final String prefix;

  private final StringSubscriber hostnameSub;
  private final StringSubscriber ipSub;
  private final StringSubscriber apiUrlSub;
  private final StringSubscriber streamsSub;
  private final StringSubscriber telemetrySub;

  public HeliosNt4Device(NetworkTableInstance inst, String prefix) {
    this.inst = Objects.requireNonNull(inst, "inst");
    this.prefix = normalizePrefix(prefix);

    hostnameSub = this.inst.getStringTopic(this.prefix + "/info/hostname").subscribe("");
    ipSub = this.inst.getStringTopic(this.prefix + "/info/ip").subscribe("");
    apiUrlSub = this.inst.getStringTopic(this.prefix + "/info/api_url").subscribe("");
    streamsSub = this.inst.getStringTopic(this.prefix + "/info/streams").subscribe("");
    telemetrySub = this.inst.getStringTopic(this.prefix + "/telemetry").subscribe("");
  }

  public static HeliosNt4Device forHostnameTable(String hostname) {
    return new HeliosNt4Device(NetworkTableInstance.getDefault(), "/" + hostname);
  }

  public String prefix() {
    return prefix;
  }

  public Optional<String> hostname() {
    String v = hostnameSub.get().trim();
    return v.isEmpty() ? Optional.empty() : Optional.of(v);
  }

  public Optional<String> ip() {
    String v = ipSub.get().trim();
    return v.isEmpty() ? Optional.empty() : Optional.of(v);
  }

  public Optional<String> apiUrl() {
    String v = apiUrlSub.get().trim();
    return v.isEmpty() ? Optional.empty() : Optional.of(v);
  }

  public Optional<JsonNode> streams() {
    return parseJson(streamsSub.get());
  }

  public Optional<JsonNode> telemetry() {
    return parseJson(telemetrySub.get());
  }

  public List<PublishedStream> parseStreams() {
    Optional<JsonNode> maybe = streams();
    if (maybe.isEmpty()) return List.of();
    JsonNode node = maybe.get();
    if (!node.isArray()) return List.of();

    List<PublishedStream> out = new ArrayList<>();
    for (JsonNode entry : node) {
      String id = entry.path("id").asText("").trim();
      if (id.isEmpty()) continue;
      UUID streamId;
      try {
        streamId = UUID.fromString(id);
      } catch (IllegalArgumentException ignored) {
        continue;
      }
      String alias = entry.path("alias").asText(null);
      String url = entry.path("url").isNull() ? null : entry.path("url").asText(null);
      out.add(new PublishedStream(streamId, alias, url));
    }
    return out;
  }

  /**
   * Same as {@link #parseStreams()}.
   *
   * <p>HeliOS backend is responsible for enforcing identity uniqueness; this client does not
   * enforce device identity rules.
   */
  @Deprecated
  public List<PublishedStream> parseStreamsStrict() {
    return parseStreams();
  }

  @Override
  public void close() {
    hostnameSub.close();
    ipSub.close();
    apiUrlSub.close();
    streamsSub.close();
    telemetrySub.close();
  }

  private static Optional<JsonNode> parseJson(String raw) {
    if (raw == null) return Optional.empty();
    String trimmed = raw.trim();
    if (trimmed.isEmpty()) return Optional.empty();
    try {
      return Optional.of(MAPPER.readTree(trimmed));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private static String normalizePrefix(String prefix) {
    String v = prefix == null ? "" : prefix.trim();
    if (v.isEmpty()) {
      throw new IllegalArgumentException("prefix is required (example: /helios-front)");
    }
    if (!v.startsWith("/")) v = "/" + v;
    while (v.endsWith("/")) v = v.substring(0, v.length() - 1);
    return v;
  }

  public static final class PublishedStream {
    public final UUID id;
    public final String alias;
    public final String url;

    public PublishedStream(UUID id, String alias, String url) {
      this.id = Objects.requireNonNull(id, "id");
      this.alias = alias;
      this.url = url;
    }
  }
}
