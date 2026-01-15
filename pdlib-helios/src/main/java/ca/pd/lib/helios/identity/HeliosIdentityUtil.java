package ca.pd.lib.helios.identity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Optional identity validation helpers.
 *
 * <p>HeliOS backend is responsible for enforcing identity uniqueness. PDLib does not use these
 * helpers to enforce identity rules in normal operation, but they can be useful for debugging
 * or validating test fixtures.
 */
public final class HeliosIdentityUtil {
  private HeliosIdentityUtil() {}

  public static <T> Map<String, T> indexStrict(
      List<T> items,
      Function<T, List<String>> tokensFn,
      Function<T, String> idFn,
      String kind) {
    Map<String, T> out = new HashMap<>();
    for (T item : items) {
      String id = idFn.apply(item);
      Map<String, Boolean> self = new HashMap<>();
      for (String rawToken : tokensFn.apply(item)) {
        String tok = rawToken == null ? "" : rawToken.trim();
        if (tok.isEmpty()) continue;
        String key = tryNormalizeUuidToken(tok).orElse(tok);

        if (self.putIfAbsent(key, Boolean.TRUE) != null) {
          throw new HeliosDuplicateIdentityException(
              kind + " identity tokens collide within " + kind + " " + id + ": token=\"" + key + "\"");
        }

        T prev = out.putIfAbsent(key, item);
        if (prev != null && !idFn.apply(prev).equals(id)) {
          throw new HeliosDuplicateIdentityException(
              "duplicate " + kind + " identity token detected: token=\"" + key + "\" " + kind + "A=" + idFn.apply(prev) + " " + kind + "B=" + id);
        }
      }
    }
    return out;
  }

  public static Optional<String> tryNormalizeUuidToken(String token) {
    try {
      return Optional.of(UUID.fromString(token).toString());
    } catch (IllegalArgumentException ignored) {
      return Optional.empty();
    }
  }
}
