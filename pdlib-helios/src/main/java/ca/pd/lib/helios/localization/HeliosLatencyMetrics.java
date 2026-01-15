package ca.pd.lib.helios.localization;

/** Best-effort latency breakdown for a solve call. */
public record HeliosLatencyMetrics(
    double httpWallMs,
    double maxSourcePollMs,
    double avgSourcePollMs,
    int sources,
    int detections) {}

