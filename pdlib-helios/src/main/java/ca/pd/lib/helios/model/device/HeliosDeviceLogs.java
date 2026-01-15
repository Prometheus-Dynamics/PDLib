package ca.pd.lib.helios.model.device;

import java.util.List;

public record HeliosDeviceLogs(List<String> lines) {
  public HeliosDeviceLogs {
    lines = lines == null ? List.of() : List.copyOf(lines);
  }
}
