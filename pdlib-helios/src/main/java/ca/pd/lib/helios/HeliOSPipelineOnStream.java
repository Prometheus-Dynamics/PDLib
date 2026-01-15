package ca.pd.lib.helios;

import ca.pd.lib.helios.model.HeliosPipelineSummary;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** A pipeline attached to a stream. */
public final class HeliOSPipelineOnStream {
  private final HeliOSStream stream;
  private final UUID pipelineId;
  private final HeliosPipelineSummary summary; // nullable if unavailable
  private final String selectedOutput; // nullable

  HeliOSPipelineOnStream(
      HeliOSStream stream,
      UUID pipelineId,
      HeliosPipelineSummary summary,
      String selectedOutput) {
    this.stream = Objects.requireNonNull(stream, "stream");
    this.pipelineId = Objects.requireNonNull(pipelineId, "pipelineId");
    this.summary = summary;
    this.selectedOutput = selectedOutput;
  }

  public UUID id() {
    return pipelineId;
  }

  public String name() {
    return summary == null ? null : summary.name();
  }

  public String selectedOutput() {
    return selectedOutput;
  }

  public HeliosPipelineSummary summary() {
    return summary;
  }

  public boolean active() throws Exception {
    UUID active = stream.info().activePipelineId();
    return active != null && active.equals(pipelineId);
  }

  public JsonNode graph() throws Exception {
    return stream.helios().pipelinesClient().getPipelineGraph(pipelineId);
  }

  public List<HeliOSOutput> outputs() throws Exception {
    requireActivePipelineForOutputs();
    return stream.outputs();
  }

  public HeliOSOutput output(String outputKey) throws Exception {
    requireActivePipelineForOutputs();
    return stream.output(outputKey);
  }

  private void requireActivePipelineForOutputs() throws Exception {
    if (active()) return;
    UUID active = stream.info().activePipelineId();
    throw new IllegalStateException(
        "pipeline is not active on stream; outputs are only available for active pipeline "
            + "(requested="
            + pipelineId
            + ", active="
            + active
            + ")");
  }
}
