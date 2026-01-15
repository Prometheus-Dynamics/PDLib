package ca.pd.lib.helios;

import ca.pd.lib.helios.http.HeliosHttp;
import ca.pd.lib.helios.model.HeliosPipelineSummary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

final class HeliOSPipelinesReadClient {
  private final HeliosHttp http;

  HeliOSPipelinesReadClient(HeliosHttp http) {
    this.http = Objects.requireNonNull(http, "http");
  }

  List<HeliosPipelineSummary> listPipelines() throws IOException, InterruptedException {
    JsonNode node = http.getJson("/v1/pipelines/graphs");
    if (!(node instanceof ArrayNode arr)) {
      throw new IOException("unexpected /v1/pipelines/graphs response shape (expected array)");
    }
    List<HeliosPipelineSummary> out = new ArrayList<>(arr.size());
    for (JsonNode entry : arr) {
      out.add(HeliosPipelineSummary.fromJson(entry));
    }
    return List.copyOf(out);
  }

  JsonNode getPipelineGraph(UUID pipelineId) throws IOException, InterruptedException {
    Objects.requireNonNull(pipelineId, "pipelineId");
    return http.getJson("/v1/pipelines/graphs/" + pipelineId);
  }

  HeliosPipelineSummary resolvePipeline(String token) throws IOException, InterruptedException {
    String key = normalizeLookupToken(token);

    List<HeliosPipelineSummary> pipelines = listPipelines();
    List<HeliosPipelineSummary> directMatches = matchPipelinesByIdOrName(pipelines, key);
    if (!directMatches.isEmpty()) {
      return requireSingleMatch(token, directMatches);
    }

    List<HeliosPipelineSummary> aliasMatches = matchPipelinesByAlias(pipelines, key);
    if (aliasMatches.isEmpty()) {
      throw new IllegalArgumentException("pipeline not found for token: " + token);
    }
    return requireSingleMatch(token, aliasMatches);
  }

  private List<HeliosPipelineSummary> matchPipelinesByIdOrName(List<HeliosPipelineSummary> pipelines, String key) {
    List<HeliosPipelineSummary> matches = new ArrayList<>();
    for (HeliosPipelineSummary pipeline : pipelines) {
      for (String raw : pipeline.tokens()) {
        if (normalizeLookupToken(raw).equals(key)) {
          matches.add(pipeline);
          break;
        }
      }
    }
    return matches;
  }

  private List<HeliosPipelineSummary> matchPipelinesByAlias(List<HeliosPipelineSummary> pipelines, String key)
      throws IOException, InterruptedException {
    List<HeliosPipelineSummary> matches = new ArrayList<>();
    for (HeliosPipelineSummary pipeline : pipelines) {
      JsonNode graphDoc = getPipelineGraph(pipeline.id());
      String alias = pipelineAlias(graphDoc);
      if (alias == null || alias.isBlank()) continue;
      if (normalizeLookupToken(alias).equals(key)) {
        matches.add(pipeline);
      }
    }
    return matches;
  }

  private static String pipelineAlias(JsonNode graphDoc) {
    if (graphDoc == null || graphDoc.isNull()) return null;
    JsonNode metadata = graphDoc.path("graph").path("metadata");
    if (metadata.isMissingNode() || metadata.isNull()) return null;

    JsonNode alias = metadata.path("helios.pipeline.alias");
    if (alias.isMissingNode() || alias.isNull()) {
      alias = metadata.path("pipeline_alias");
    }
    String out = alias.asText("").trim();
    return out.isEmpty() ? null : out;
  }

  private static HeliosPipelineSummary requireSingleMatch(String token, List<HeliosPipelineSummary> matches) {
    if (matches.size() > 1) {
      String ids =
          matches.stream()
              .map(pipeline -> pipeline.id().toString())
              .distinct()
              .limit(5)
              .reduce((a, b) -> a + ", " + b)
              .orElse("");
      throw new IllegalStateException(
          "ambiguous pipeline token (matches "
              + matches.size()
              + " pipelines): token="
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
