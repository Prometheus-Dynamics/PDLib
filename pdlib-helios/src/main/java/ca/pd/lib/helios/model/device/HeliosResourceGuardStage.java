package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum HeliosResourceGuardStage {
  @JsonProperty("decoder_disabled")
  DECODER_DISABLED,
  @JsonProperty("codecs_disabled")
  CODECS_DISABLED
}
