package ca.pd.lib.helios;

import ca.pd.lib.core.json.JsonSupport;
import ca.pd.lib.helios.api.HeliosDeviceReadApi;
import ca.pd.lib.helios.api.HeliosDeviceReadClient;
import ca.pd.lib.helios.http.HeliosHttp;
import ca.pd.lib.helios.localization.HeliosLocalizationConfig;
import ca.pd.lib.helios.localization.HeliosLocalizationProfile;
import ca.pd.lib.helios.model.HeliosPipelineSummary;
import ca.pd.lib.helios.model.HeliosStreamInfo;
import ca.pd.lib.helios.util.HeliosUrls;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Read-only fluent entry point for HeliOS device APIs. */
public final class HeliOS {
  private static final Duration DEFAULT_TIMEOUT = resolveDefaultTimeout();

  private final URI baseUri;
  private final HeliosHttp http;
  private final HeliOSStreamsReadClient streams;
  private final HeliOSPipelinesReadClient pipelines;
  private final HeliOSLocalizationReadClient localization;
  private final HeliosDeviceReadApi deviceRead;

  public HeliOS(String hostOrIpOrNtName) {
    Objects.requireNonNull(hostOrIpOrNtName, "hostOrIpOrNtName");
    this.baseUri = HeliosUrls.baseUri(hostOrIpOrNtName);
    HttpClient client =
        HttpClient.newBuilder()
            .connectTimeout(DEFAULT_TIMEOUT)
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    this.http = new HeliosHttp(baseUri, client, JsonSupport.MAPPER, DEFAULT_TIMEOUT);
    this.streams = new HeliOSStreamsReadClient(this.http);
    this.pipelines = new HeliOSPipelinesReadClient(this.http);
    this.localization = new HeliOSLocalizationReadClient(this.http);
    this.deviceRead = new HeliosDeviceReadClient(this.http);
  }

  public URI baseUri() {
    return baseUri;
  }

  /** Typed read-only device section. */
  public HeliosDeviceReadApi device() {
    return deviceRead;
  }

  public List<HeliOSStream> streams() throws Exception {
    List<HeliosStreamInfo> infos = streams.listStreams();
    List<HeliOSStream> out = new ArrayList<>(infos.size());
    for (HeliosStreamInfo info : infos) {
      out.add(new HeliOSStream(this, info.id(), info));
    }
    return List.copyOf(out);
  }

  public HeliOSStream stream(String token) throws Exception {
    HeliosStreamInfo info = streams.resolveStream(token);
    return new HeliOSStream(this, info.id(), info);
  }

  public Optional<HeliOSStream> findStream(String token) {
    try {
      return Optional.of(stream(token));
    } catch (Exception ignored) {
      return Optional.empty();
    }
  }

  public List<HeliosLocalizationProfile> localizations() throws Exception {
    HeliosLocalizationConfig cfg = localization.getConfig();
    List<HeliosLocalizationProfile> profiles = cfg.profiles() == null ? List.of() : cfg.profiles();
    return List.copyOf(profiles);
  }

  /** Backend default localization profile. */
  public HeliOSLocalization localization() {
    return new HeliOSLocalization(this, null);
  }

  public HeliOSLocalization localization(String profileToken) throws Exception {
    String token = normalizeToken(profileToken);
    for (HeliosLocalizationProfile profile : localizations()) {
      if (profile == null) continue;
      if (token.equals(profile.id())) {
        return new HeliOSLocalization(this, profile.id());
      }
      String name = profile.name();
      if (name != null && token.equalsIgnoreCase(name.trim())) {
        return new HeliOSLocalization(this, profile.id());
      }
    }
    throw new IllegalArgumentException("localization profile not found for token: " + profileToken);
  }

  public List<HeliOSPipeline> pipelines() throws Exception {
    List<HeliosPipelineSummary> summaries = pipelines.listPipelines();
    List<HeliOSPipeline> out = new ArrayList<>(summaries.size());
    for (HeliosPipelineSummary summary : summaries) {
      out.add(new HeliOSPipeline(this, summary));
    }
    return List.copyOf(out);
  }

  public HeliOSPipeline pipeline(String token) throws Exception {
    HeliosPipelineSummary summary = pipelines.resolvePipeline(token);
    return new HeliOSPipeline(this, summary);
  }

  public Optional<HeliOSPipeline> findPipeline(String token) {
    try {
      return Optional.of(pipeline(token));
    } catch (Exception ignored) {
      return Optional.empty();
    }
  }

  public HeliOSPeripherals peripherals() {
    return new HeliOSPeripherals(this);
  }

  /** Compatibility alias for common misspelling. */
  public HeliOSPeripherals peripherials() {
    return peripherals();
  }

  HeliosHttp http() {
    return http;
  }

  HeliOSStreamsReadClient streamsClient() {
    return streams;
  }

  HeliOSPipelinesReadClient pipelinesClient() {
    return pipelines;
  }

  HeliOSLocalizationReadClient localizationClient() {
    return localization;
  }

  private static String normalizeToken(String token) {
    String v = token == null ? "" : token.trim();
    if (v.isEmpty()) throw new IllegalArgumentException("token is required");
    return v;
  }

  private static Duration resolveDefaultTimeout() {
    int seconds = 8;
    String fromProperty = System.getProperty("helios.timeoutSec");
    String fromEnv = System.getenv("HELIOS_TIMEOUT_SEC");
    String raw = fromProperty != null ? fromProperty : fromEnv;
    if (raw != null && !raw.trim().isEmpty()) {
      try {
        seconds = Integer.parseInt(raw.trim());
      } catch (NumberFormatException ignored) {
        // Use default.
      }
    }
    seconds = Math.max(1, Math.min(60, seconds));
    return Duration.ofSeconds(seconds);
  }
}
