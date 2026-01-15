package ca.pd.lib.helios.util;

import java.net.URI;
import java.util.Objects;

/** Small helpers for turning user input (hostname/ip/url) into a HeliOS base URL. */
public final class HeliosUrls {
  private HeliosUrls() {}

  /**
   * Convert {@code hostOrIpOrUrl} into a base URI.
   *
   * <p>Rules:
   * - If it starts with {@code http://} or {@code https://}, use it as-is.
   * - If it looks like {@code host:port}, assume {@code http://}.
   * - Otherwise assume {@code http://<host>:5800}.
   */
  public static URI baseUri(String hostOrIpOrUrl) {
    Objects.requireNonNull(hostOrIpOrUrl, "hostOrIpOrUrl");
    String v = hostOrIpOrUrl.trim();
    if (v.isEmpty()) throw new IllegalArgumentException("hostOrIpOrUrl is required");

    if (v.startsWith("http://") || v.startsWith("https://")) {
      while (v.endsWith("/")) v = v.substring(0, v.length() - 1);
      return URI.create(v);
    }

    // If user passed host:port, keep it.
    if (v.contains(":")) {
      return URI.create("http://" + v);
    }

    return URI.create("http://" + v + ":5800");
  }
}

