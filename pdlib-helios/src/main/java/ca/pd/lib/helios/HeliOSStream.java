package ca.pd.lib.helios;

import ca.pd.lib.helios.model.HeliosPipelineOutputDescriptor;
import ca.pd.lib.helios.model.HeliosPipelineSummary;
import ca.pd.lib.helios.model.HeliosStreamInfo;
import ca.pd.lib.helios.model.HeliosStreamPipelineBinding;
import ca.pd.lib.helios.model.HeliosStreamControl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Stream wrapper for read-only fluent API. */
public final class HeliOSStream {
  private final HeliOS helios;
  private final UUID id;
  private HeliosStreamInfo cachedInfo;

  HeliOSStream(HeliOS helios, UUID id, HeliosStreamInfo cachedInfo) {
    this.helios = Objects.requireNonNull(helios, "helios");
    this.id = Objects.requireNonNull(id, "id");
    this.cachedInfo = cachedInfo;
  }

  HeliOS helios() {
    return helios;
  }

  public UUID id() {
    return id;
  }

  /** Latest stream info from {@code /v1/streams/{id}}. */
  public HeliosStreamInfo info() throws Exception {
    cachedInfo = helios.streamsClient().getStream(id);
    return cachedInfo;
  }

  /** Last fetched stream info without forcing a refresh. */
  public HeliosStreamInfo cachedInfo() {
    return cachedInfo;
  }

  public JsonNode settings() throws Exception {
    return info().raw().path("manifest");
  }

  public JsonNode layout() throws Exception {
    return settings().path("pipeline_layout");
  }

  public JsonNode pose() throws Exception {
    return settings().path("pose");
  }

  public JsonNode calibration() throws Exception {
    return settings().path("calibration");
  }

  public JsonNode format() throws Exception {
    try {
      return helios.streamsClient().getStreamFormat(id);
    } catch (IOException ex) {
      if (isNotFound(ex)) {
        return MissingNode.getInstance();
      }
      throw ex;
    }
  }

  public JsonNode metrics() throws Exception {
    try {
      return helios.streamsClient().getStreamMetrics(id);
    } catch (IOException ex) {
      if (isNotFound(ex)) {
        return MissingNode.getInstance();
      }
      throw ex;
    }
  }

  public List<HeliosStreamControl> controls() throws Exception {
    try {
      return helios.streamsClient().listControls(id);
    } catch (IOException ex) {
      if (isNotFound(ex)) {
        return List.of();
      }
      throw ex;
    }
  }

  public HeliosStreamControl control(String token) throws Exception {
    return helios.streamsClient().resolveControl(id, token);
  }

  public Optional<HeliosStreamControl> findControl(String token) {
    try {
      return Optional.of(control(token));
    } catch (Exception ignored) {
      return Optional.empty();
    }
  }

  public List<HeliOSOutput> outputs() throws Exception {
    List<HeliosPipelineOutputDescriptor> descriptors;
    try {
      descriptors = helios.streamsClient().listPipelineOutputDescriptors(id);
    } catch (IOException ex) {
      if (isNotFound(ex)) {
        return List.of();
      }
      throw ex;
    }
    List<HeliOSOutput> out = new ArrayList<>(descriptors.size());
    for (HeliosPipelineOutputDescriptor descriptor : descriptors) {
      out.add(new HeliOSOutput(helios, id, descriptor));
    }
    return List.copyOf(out);
  }

  public HeliOSOutput output(String outputKey) throws Exception {
    String key = normalizeToken(outputKey);
    for (HeliOSOutput output : outputs()) {
      if (output.key().equalsIgnoreCase(key)) {
        return output;
      }
    }
    throw new IllegalArgumentException("output not found for key: " + outputKey);
  }

  public List<HeliOSPipelineOnStream> pipelines() throws Exception {
    HeliosStreamInfo stream = info();
    Map<UUID, String> selectedOutputByPipeline = new LinkedHashMap<>();
    Set<UUID> pipelineIds = new LinkedHashSet<>();

    for (HeliosStreamPipelineBinding binding : stream.pipelines()) {
      if (binding == null) continue;
      pipelineIds.add(binding.pipelineId());
      selectedOutputByPipeline.put(binding.pipelineId(), binding.pipelineOutput());
    }

    if (stream.activePipelineId() != null) {
      pipelineIds.add(stream.activePipelineId());
      selectedOutputByPipeline.putIfAbsent(stream.activePipelineId(), stream.activePipelineOutput());
    }

    Map<UUID, HeliosPipelineSummary> summaryById = new LinkedHashMap<>();
    for (HeliosPipelineSummary summary : helios.pipelinesClient().listPipelines()) {
      summaryById.put(summary.id(), summary);
    }

    List<HeliOSPipelineOnStream> out = new ArrayList<>(pipelineIds.size());
    for (UUID pipelineId : pipelineIds) {
      HeliosPipelineSummary summary = summaryById.get(pipelineId);
      String selectedOutput = selectedOutputByPipeline.get(pipelineId);
      out.add(new HeliOSPipelineOnStream(this, pipelineId, summary, selectedOutput));
    }
    return List.copyOf(out);
  }

  /** Active pipeline on this stream. */
  public HeliOSPipelineOnStream pipeline() throws Exception {
    HeliosStreamInfo stream = info();
    UUID active = stream.activePipelineId();
    if (active == null) {
      throw new IllegalStateException("stream has no active pipeline");
    }
    return pipelineById(active);
  }

  /** Attached pipeline by token (uuid/name/alias). */
  public HeliOSPipelineOnStream pipeline(String token) throws Exception {
    String key = normalizeToken(token);
    Optional<UUID> asUuid = tryParseUuid(key);
    if (asUuid.isPresent()) {
      try {
        return pipelineById(asUuid.get());
      } catch (IllegalArgumentException ignored) {
        // Fall back to global pipeline resolution.
      }
    }
    HeliosPipelineSummary resolved = helios.pipelinesClient().resolvePipeline(key);
    return pipelineById(resolved.id());
  }

  private HeliOSPipelineOnStream pipelineById(UUID pipelineId) throws Exception {
    Objects.requireNonNull(pipelineId, "pipelineId");
    for (HeliOSPipelineOnStream pipeline : pipelines()) {
      if (pipeline.id().equals(pipelineId)) {
        return pipeline;
      }
    }
    throw new IllegalArgumentException(
        "pipeline is not attached to this stream: stream="
            + id
            + " pipeline="
            + pipelineId);
  }

  private static String normalizeToken(String token) {
    String out = token == null ? "" : token.trim();
    if (out.isEmpty()) throw new IllegalArgumentException("token is required");
    return out;
  }

  private static Optional<UUID> tryParseUuid(String token) {
    try {
      return Optional.of(UUID.fromString(token));
    } catch (IllegalArgumentException ignored) {
      return Optional.empty();
    }
  }

  private static boolean isNotFound(IOException ex) {
    String msg = ex.getMessage();
    return msg != null && msg.contains(" failed: 404");
  }
}
