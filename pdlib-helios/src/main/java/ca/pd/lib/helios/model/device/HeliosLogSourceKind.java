package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum HeliosLogSourceKind {
  @JsonProperty("journal_system")
  JOURNAL_SYSTEM,
  @JsonProperty("journal_unit")
  JOURNAL_UNIT,
  @JsonProperty("dmesg")
  DMESG,
  @JsonProperty("file")
  FILE
}
