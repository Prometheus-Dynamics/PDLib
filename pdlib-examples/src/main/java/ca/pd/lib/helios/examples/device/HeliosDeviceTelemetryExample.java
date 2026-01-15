package ca.pd.lib.helios.examples.device;

import ca.pd.lib.helios.HeliOS;

/** Example: basic device telemetry and peripheral status reads. */
public final class HeliosDeviceTelemetryExample {
  private HeliosDeviceTelemetryExample() {}

  public static void main(String[] args) throws Exception {
    String target = args.length > 0 ? args[0] : "172.31.250.1";
    HeliOS cam = new HeliOS(target);

    System.out.println("health=" + cam.device().health());
    System.out.println("metrics=" + cam.device().metrics());
    System.out.println("power=" + cam.peripherals().power());
    System.out.println("imu=" + cam.peripherals().imu());
    System.out.println("fan=" + cam.peripherals().fan());
    System.out.println("leds=" + cam.peripherals().leds());
    System.out.println("nt4=" + cam.device().nt4());
  }
}
