package ca.pd.lib.helios.examples.device;

import ca.pd.lib.helios.HeliOS;

/** Example: read fan/peripheral status. */
public final class HeliosFanExample {
  private HeliosFanExample() {}

  public static void main(String[] args) throws Exception {
    String target = args.length > 0 ? args[0] : "172.31.250.1";
    HeliOS cam = new HeliOS(target);

    System.out.println("fan=" + cam.peripherals().fan());
  }
}
