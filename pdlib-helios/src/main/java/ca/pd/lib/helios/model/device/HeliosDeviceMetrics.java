package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosDeviceMetrics(
    @JsonProperty("cpu_avg_pct") float cpuAvgPct,
    @JsonProperty("cpu_freq_mhz") long cpuFreqMhz,
    List<HeliosCpuCoreMetrics> cpus,
    @JsonProperty("mem_total_bytes") long memTotalBytes,
    @JsonProperty("mem_used_bytes") long memUsedBytes,
    @JsonProperty("swap_total_bytes") long swapTotalBytes,
    @JsonProperty("swap_used_bytes") long swapUsedBytes,
    List<HeliosDiskMetrics> disks,
    List<HeliosTempReading> temps) {}
