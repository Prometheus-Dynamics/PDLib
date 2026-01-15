package ca.pd.lib.helios;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;

/** Read-only wrapper for peripheral/device telemetry endpoints. */
public final class HeliOSPeripherals {
  private final HeliOS helios;

  HeliOSPeripherals(HeliOS helios) {
    this.helios = Objects.requireNonNull(helios, "helios");
  }

  public JsonNode all() throws Exception {
    return helios.http().getJson("/v1/peripherals");
  }

  public JsonNode cameras() throws Exception {
    return helios.http().getJson("/v1/peripherals/cameras");
  }

  public JsonNode i2c() throws Exception {
    return helios.http().getJson("/v1/peripherals/i2c");
  }

  public JsonNode usb() throws Exception {
    return helios.http().getJson("/v1/peripherals/usb");
  }

  public JsonNode sensors() throws Exception {
    return helios.http().getJson("/v1/peripherals/sensors");
  }

  public JsonNode imu() throws Exception {
    return helios.http().getJson("/v1/device/imu");
  }

  public JsonNode fan() throws Exception {
    return helios.http().getJson("/v1/peripherals/fan");
  }

  public JsonNode leds() throws Exception {
    return helios.http().getJson("/v1/peripherals/leds");
  }

  public JsonNode power() throws Exception {
    return helios.http().getJson("/v1/device/power");
  }
}
