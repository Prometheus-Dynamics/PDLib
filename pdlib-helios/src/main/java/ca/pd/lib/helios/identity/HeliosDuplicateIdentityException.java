package ca.pd.lib.helios.identity;

/** Thrown when HeliOS reports identities that violate PDLib's uniqueness constraints. */
public final class HeliosDuplicateIdentityException extends IllegalStateException {
  public HeliosDuplicateIdentityException(String message) {
    super(message);
  }
}

