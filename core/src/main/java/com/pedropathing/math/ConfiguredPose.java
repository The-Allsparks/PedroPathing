package com.pedropathing.math;

import com.pedropathing.geometry.CoordinateSystem;
import com.pedropathing.geometry.PedroCoordinates;
import com.pedropathing.geometry.Pose;

/**
 * Immutable display/interface pose whose x/y/heading are expressed in a {@link PedroUnits}
 * configuration.
 *
 * <p>This is not a Pedro {@link Pose}. Do not pass it into follower math, path construction, or
 * localizers. Convert with {@link #toInternalPose()} first.
 *
 * @author The Allsparks - 36117
 */
public final class ConfiguredPose {
    private final double x;
    private final double y;
    private final double heading;
    private final PedroUnits units;
    private final CoordinateSystem coordinateSystem;

    ConfiguredPose(double x, double y, double heading, PedroUnits units, CoordinateSystem coordinateSystem) {
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.units = PedroUnits.requireNonNull(units);
        this.coordinateSystem = coordinateSystem == null ? PedroCoordinates.INSTANCE : coordinateSystem;
    }

    /** X in the configured length unit. */
    public double x() {
        return x;
    }

    /** Y in the configured length unit. */
    public double y() {
        return y;
    }

    /** Heading in the configured angle unit. */
    public double heading() {
        return heading;
    }

    public PedroUnits units() {
        return units;
    }

    public LengthUnit lengthUnit() {
        return units.lengthUnit();
    }

    public AngularUnit angleUnit() {
        return units.angleUnit();
    }

    public CoordinateSystem coordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Convert this configured pose into a canonical Pedro {@link Pose} (inches and radians).
     */
    public Pose toInternalPose() {
        return new Pose(
                units.lengthToInternal(x),
                units.lengthToInternal(y),
                units.angleToInternal(heading),
                coordinateSystem);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ConfiguredPose)) {
            return false;
        }
        ConfiguredPose pose = (ConfiguredPose) other;
        return Double.compare(pose.x, x) == 0
                && Double.compare(pose.y, y) == 0
                && Double.compare(pose.heading, heading) == 0
                && units.equals(pose.units)
                && coordinateSystem.equals(pose.coordinateSystem);
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(heading);
        result = 31 * result + units.hashCode();
        result = 31 * result + coordinateSystem.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ConfiguredPose("
                + x + " " + units.lengthUnit().symbol()
                + ", " + y + " " + units.lengthUnit().symbol()
                + ", " + heading + " " + units.angleUnit().symbol()
                + ")";
    }
}
