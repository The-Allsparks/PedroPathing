package com.pedropathing.math;

import com.pedropathing.geometry.Pose;

/**
 * Immutable follower length-unit context. Store this on a follower configuration rather than
 * mutating process-wide state.
 *
 * <p>Two contexts with different units can coexist. Creating or using one context never changes
 * another. Class loading does not select a unit.
 *
 * @author The Allsparks - 36117
 */
public final class LengthContext {
    private static final LengthContext[] CACHE = createCache();

    public static final LengthContext INCHES = CACHE[LengthUnit.INCHES.ordinal()];
    public static final LengthContext FEET = CACHE[LengthUnit.FEET.ordinal()];
    public static final LengthContext CENTIMETERS = CACHE[LengthUnit.CENTIMETERS.ordinal()];
    public static final LengthContext METERS = CACHE[LengthUnit.METERS.ordinal()];

    private final LengthUnit unit;

    private LengthContext(LengthUnit unit) {
        this.unit = unit;
    }

    private static LengthContext[] createCache() {
        LengthContext[] cache = new LengthContext[LengthUnit.values().length];
        for (LengthUnit unit : LengthUnit.values()) {
            cache[unit.ordinal()] = new LengthContext(unit);
        }
        return cache;
    }

    public static LengthContext of(LengthUnit unit) {
        return CACHE[LengthUnit.requireNonNull(unit).ordinal()];
    }

    public LengthUnit unit() {
        return unit;
    }

    public double fromInches(double inches) {
        return unit.fromInches(inches);
    }

    public double toInches(double value) {
        return unit.toInches(value);
    }

    public double convert(double value, LengthUnit from) {
        return LengthUnit.rescale(value, from, unit);
    }

    public double convertTo(double value, LengthUnit to) {
        return unit.convert(value, to);
    }

    public double tile() {
        return unit.tile();
    }

    public double fieldSize() {
        return unit.fieldSize();
    }

    public double fieldCenter() {
        return unit.fieldCenter();
    }

    public double mirrorFieldLength() {
        return unit.mirrorFieldLength();
    }

    /**
     * Convert a pose whose x/y are in {@code from} into this context. Heading is unchanged.
     */
    public Pose convertPose(Pose pose, LengthUnit from) {
        if (pose == null) {
            return null;
        }
        LengthUnit.requireNonNull(from);
        if (from == unit) {
            return pose.copy();
        }
        return new Pose(
                convert(pose.getX(), from),
                convert(pose.getY(), from),
                pose.getHeading(),
                pose.getCoordinateSystem());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LengthContext)) {
            return false;
        }
        return unit == ((LengthContext) other).unit;
    }

    @Override
    public int hashCode() {
        return unit.hashCode();
    }

    @Override
    public String toString() {
        return "LengthContext(" + unit.pluralName() + ")";
    }
}
