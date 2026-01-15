package ca.pd.lib.helios.examples.localization;

import ca.pd.lib.helios.HeliOS;
import ca.pd.lib.helios.localization.HeliosLocalizationProfile;
import java.util.List;

/** Example: list localization profiles (from /v1/localization/config). */
public final class HeliosLocalizationProfilesExample {
  private HeliosLocalizationProfilesExample() {}

  public static void main(String[] args) throws Exception {
    String target = args.length > 0 ? args[0] : "172.31.250.1";
    HeliOS cam = new HeliOS(target);

    List<HeliosLocalizationProfile> profiles = cam.localizations();
    System.out.println("profiles=" + profiles.size());
    for (HeliosLocalizationProfile profile : profiles) {
      if (profile == null) continue;
      System.out.println(" - id=" + profile.id() + " name=" + profile.name());
    }
  }
}
