package ca.pd.lib.helios.ws;

import java.net.URI;

final class HeliosWsUtil {
  private HeliosWsUtil() {}

  static URI toWebSocketUri(URI baseUri, String pathAndQuery) {
    String scheme = baseUri.getScheme();
    String wsScheme;
    if ("https".equalsIgnoreCase(scheme)) {
      wsScheme = "wss";
    } else if ("http".equalsIgnoreCase(scheme)) {
      wsScheme = "ws";
    } else {
      throw new IllegalArgumentException("unsupported base URI scheme: " + scheme);
    }

    String base = baseUri.toString();
    // baseUri is expected to be normalized without trailing '/'.
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }

    String suffix = pathAndQuery.startsWith("/") ? pathAndQuery : "/" + pathAndQuery;
    return URI.create(wsScheme + base.substring(scheme.length()) + suffix);
  }
}
