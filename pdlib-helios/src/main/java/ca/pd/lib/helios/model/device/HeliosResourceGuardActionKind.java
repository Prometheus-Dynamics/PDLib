package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum HeliosResourceGuardActionKind {
  @JsonProperty("disable_decoder")
  DISABLE_DECODER,
  @JsonProperty("disable_all_codecs")
  DISABLE_ALL_CODECS,
  @JsonProperty("stop_stream")
  STOP_STREAM,
  @JsonProperty("restore_codecs")
  RESTORE_CODECS
}
