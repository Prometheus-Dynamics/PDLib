package ca.pd.lib.helios.vision;

import java.util.List;

/** 2D fiducial detection (pixel-space corners + id), as exposed by common HeliOS pipelines. */
public record HeliosFiducialDetection2d(int id, int rotation, List<HeliosPoint2d> corners) {
  public HeliosPoint2d center() {
    if (corners == null || corners.isEmpty()) return new HeliosPoint2d(Double.NaN, Double.NaN);
    double sx = 0.0, sy = 0.0;
    for (HeliosPoint2d p : corners) {
      sx += p.x();
      sy += p.y();
    }
    return new HeliosPoint2d(sx / corners.size(), sy / corners.size());
  }
}

