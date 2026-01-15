package ca.pd.lib.helios.examples.pipelines;

import ca.pd.lib.helios.HeliOS;
import ca.pd.lib.helios.HeliOSPipeline;
import ca.pd.lib.helios.model.HeliosPipelineSummary;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/** Example: list pipelines, resolve a pipeline by uuid/name/alias, and fetch its graph document. */
public final class HeliosPipelinesLookupExample {
  private HeliosPipelinesLookupExample() {}

  public static void main(String[] args) throws Exception {
    String target = args.length > 0 ? args[0] : "172.31.250.1";
    String pipelineToken = args.length > 1 ? args[1] : null;

    HeliOS cam = new HeliOS(target);

    List<HeliOSPipeline> pipelines = cam.pipelines();
    System.out.println("pipelines=" + pipelines.size());
    for (int i = 0; i < Math.min(5, pipelines.size()); i++) {
      HeliosPipelineSummary summary = pipelines.get(i).summary();
      System.out.println(
          " - id=" + summary.id() + " name=" + summary.name() + " updated_at_ms=" + summary.updatedAtMs());
    }

    if (pipelineToken == null || pipelineToken.isBlank()) return;

    HeliOSPipeline pipeline = cam.pipeline(pipelineToken);
    JsonNode graphDoc = pipeline.graph();
    System.out.println("resolved_pipeline_id=" + pipeline.id() + " name=" + pipeline.name());
    System.out.println("graph_doc_keys=" + (graphDoc.isObject() && graphDoc.fieldNames().hasNext()
        ? graphDoc.fieldNames().next()
        : "<none>"));
  }
}
