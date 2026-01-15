package ca.pd.lib.core.json;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public final class JsonSupport {
  public static final ObjectMapper MAPPER =
      JsonMapper.builder()
          .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
          .build();

  private JsonSupport() {}
}
