package ca.pd.lib.helios;

import ca.pd.lib.helios.model.HeliosPipelineSummary;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import java.util.UUID;

/** Global stored pipeline graph reference. */
public final class HeliOSPipeline {
  private final HeliOS helios;
  private final HeliosPipelineSummary summary;

  HeliOSPipeline(HeliOS helios, HeliosPipelineSummary summary) {
    this.helios = Objects.requireNonNull(helios, "helios");
    this.summary = Objects.requireNonNull(summary, "summary");
  }

  public UUID id() {
    return summary.id();
  }

  public String name() {
    return summary.name();
  }

  public HeliosPipelineSummary summary() {
    return summary;
  }

  public JsonNode graph() throws Exception {
    return helios.pipelinesClient().getPipelineGraph(summary.id());
  }
}
