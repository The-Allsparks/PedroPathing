package com.pedropathing.math;

/**
 * Selectable length unit for Pedro Pathing poses, paths, localizer constants, and tuners.
 *
 * <p>The follower math is unit-agnostic: pick one unit and use it everywhere. Inches remain the
 * default so upstream Quickstart projects keep their existing numbers. Inch-authored defaults live
 * in {@link LengthAnchors} and are converted with {@link #rescale}, {@link #rescaleInverse}, and
 * {@link #rescaleSquared}. Hardware localizers that talk to FTC {@code DistanceUnit} are mapped
 * from this value in the FTC adapter.
 *
 * <p>Pick the unit once with {@link #use(LengthUnit)} or {@link #setActive(LengthUnit)}. Constants
 * constructors, tuners, and hardware adapters look up {@link #active()} instead of taking a unit
 * argument.
 *
 * @author The Allsparks - 36117
 */
public enum LengthUnit {
    INCHES("in", "inches", 1.0),
    FEET("ft", "feet", 1.0 / 12.0),
    CENTIMETERS("cm", "centimeters", 2.54),
    MILLIMETERS("mm", "millimeters", 25.4),
    METERS("m", "meters", 0.0254);

    /** Official FTC field tile edge, in inches. */
    public static final double TILE_INCHES = 24.0;

    /** Official FTC field size, in inches. */
    public static final double FIELD_INCHES = 144.0;

    /**
     * Default Pedro {@code Pose#mirror()} field length, in inches.
     * Upstream uses 141.5 rather than a full 144.
     */
    public static final double MIRROR_FIELD_INCHES = 141.5;

    private static volatile LengthUnit active = INCHES;

    private final String symbol;
    private final String pluralName;
    private final double unitsPerInch;

    LengthUnit(String symbol, String pluralName, double unitsPerInch) {
        this.symbol = symbol;
        this.pluralName = pluralName;
        this.unitsPerInch = unitsPerInch;
    }

    public String symbol() {
        return symbol;
    }

    public String pluralName() {
        return pluralName;
    }

    /** Convert a value in inches into this unit. */
    public double fromInches(double inches) {
        return inches * unitsPerInch;
    }

    /** Convert a value in this unit into inches. */
    public double toInches(double value) {
        return value / unitsPerInch;
    }

    /** Convert a value in this unit into millimeters. */
    public double toMillimeters(double value) {
        return toInches(value) * 25.4;
    }

    /** Convert millimeters into this unit. */
    public double fromMillimeters(double millimeters) {
        return fromInches(millimeters / 25.4);
    }

    /** Convert a value in this unit into {@code to}. */
    public double convert(double value, LengthUnit to) {
        if (this == to) {
            return value;
        }
        return to.fromInches(toInches(value));
    }

    public double tile() {
        return fromInches(TILE_INCHES);
    }

    public double fieldSize() {
        return fromInches(FIELD_INCHES);
    }

    public double fieldCenter() {
        return fromInches(FIELD_INCHES / 2.0);
    }

    public double mirrorFieldLength() {
        return fromInches(MIRROR_FIELD_INCHES);
    }

    /**
     * Multiply this unit's values by this to get inches, or divide inches by this to get this unit.
     * Equal to {@link #fromInches(double) fromInches(1)}.
     */
    public double unitsPerInch() {
        return unitsPerInch;
    }

    /** Convert a length, speed, or acceleration from {@code from} into {@code to}. */
    public static double rescale(double value, LengthUnit from, LengthUnit to) {
        if (from == to) {
            return value;
        }
        return to.fromInches(from.toInches(value));
    }

    /**
     * Convert a per-length quantity (PID P/I/D/F on a length error, centripetal scaling)
     * from {@code from} into {@code to}.
     */
    public static double rescaleInverse(double value, LengthUnit from, LengthUnit to) {
        if (from == to) {
            return value;
        }
        return value * from.unitsPerInch / to.unitsPerInch;
    }

    /** Convert a length-squared quantity (1D Kalman covariance on a length error). */
    public static double rescaleSquared(double value, LengthUnit from, LengthUnit to) {
        if (from == to) {
            return value;
        }
        double factor = to.unitsPerInch / from.unitsPerInch;
        return value * factor * factor;
    }

    public static LengthUnit active() {
        return active;
    }

    /**
     * Set the process-wide length unit and return it so TeamCode can store it on the constants
     * class: {@code public static final LengthUnit LENGTH = LengthUnit.use(LengthUnit.CENTIMETERS);}
     */
    public static LengthUnit use(LengthUnit unit) {
        setActive(unit);
        return unit;
    }

    public static void setActive(LengthUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("length unit must not be null");
        }
        active = unit;
    }

    /** Convert inches into {@link #active()}. */
    public static double ofInches(double inches) {
        return active().fromInches(inches);
    }

    /** Convert a value in {@link #active()} into inches. */
    public static double inInches(double value) {
        return active().toInches(value);
    }

    /** Convert a length, speed, or acceleration from {@code from} into {@link #active()}. */
    public static double rescale(double value, LengthUnit from) {
        return rescale(value, from, active());
    }

    /**
     * Convert a per-length quantity from {@code from} into {@link #active()}.
     */
    public static double rescaleInverse(double value, LengthUnit from) {
        return rescaleInverse(value, from, active());
    }

    /** Convert a length-squared quantity from {@code from} into {@link #active()}. */
    public static double rescaleSquared(double value, LengthUnit from) {
        return rescaleSquared(value, from, active());
    }
}
