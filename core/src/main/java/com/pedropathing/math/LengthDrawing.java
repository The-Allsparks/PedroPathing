package com.pedropathing.math;

import com.pedropathing.geometry.Pose;

/**
 * Converts follower-unit geometry into inches for inch-based drawing surfaces such as Panels Field.
 *
 * <p>Call these helpers only at the drawing boundary. Do not pass the converted pose or points
 * back into follower or path logic.
 *
 * @author The Allsparks - 36117
 */
public final class LengthDrawing {
    private LengthDrawing() {}

    public static double toInches(double value, LengthUnit followerUnit) {
        return LengthUnit.requireNonNull(followerUnit).toInches(value);
    }

    public static Pose toInches(Pose pose, LengthUnit followerUnit) {
        if (pose == null) {
            return null;
        }
        LengthUnit.requireNonNull(followerUnit);
        if (followerUnit == LengthUnit.INCHES) {
            return pose;
        }
        return new Pose(
                followerUnit.toInches(pose.getX()),
                followerUnit.toInches(pose.getY()),
                pose.getHeading(),
                pose.getCoordinateSystem());
    }

    /**
     * Convert Panels path-drawing points from the follower unit into inches, in place.
     * The array layout matches {@code Path#getPanelsDrawingPoints()}.
     */
    public static double[][] pathPointsToInches(double[][] points, LengthUnit followerUnit) {
        if (points == null) {
            return null;
        }
        LengthUnit.requireNonNull(followerUnit);
        if (followerUnit == LengthUnit.INCHES) {
            return points;
        }
        for (int i = 0; i < points.length; i++) {
            if (points[i] == null) {
                continue;
            }
            for (int j = 0; j < points[i].length; j++) {
                if (!Double.isNaN(points[i][j])) {
                    points[i][j] = followerUnit.toInches(points[i][j]);
                }
            }
        }
        return points;
    }
}
