package com.pedropathing.math;

/**
 * Selectable follower length unit for Pedro Pathing poses, paths, localizer constants, and tuners.
 *
 * <p>The follower math is unit-agnostic: pick one unit and use it everywhere. Inches remain the
 * default so upstream Quickstart projects keep their existing numbers. Inch-authored defaults live
 * in {@link LengthAnchors} and are converted with {@link #rescale}, {@link #rescaleInverse}, and
 * {@link #rescaleSquared}.
 *
 * <p>Millimeters are not a selectable follower unit. Hardware that reports millimeters (OctoQuad)
 * should use {@link #toMillimeters(double)} and {@link #fromMillimeters(double)} at the adapter
 * boundary.
 *
 * <p>The selected unit belongs to a {@link LengthContext} or follower configuration. This enum
 * has no process-wide mutable state: constructing one follower cannot change another, and loading
 * this class does not alter any unit system.
 *
 * @author The Allsparks - 36117
 */
public enum LengthUnit {
    INCHES("in", "inches", 1.0),
    FEET("ft", "feet", 1.0 / 12.0),
    CENTIMETERS("cm", "centimeters", 2.54),
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

    private static final double MILLIMETERS_PER_INCH = 25.4;

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

    /**
     * Convert a value in this unit into millimeters. Millimeters are a hardware-boundary unit,
     * not a selectable follower unit.
     */
    public double toMillimeters(double value) {
        return toInches(value) * MILLIMETERS_PER_INCH;
    }

    /**
     * Convert millimeters into this unit. Millimeters are a hardware-boundary unit,
     * not a selectable follower unit.
     */
    public double fromMillimeters(double millimeters) {
        return fromInches(millimeters / MILLIMETERS_PER_INCH);
    }

    /** Convert a value in this unit into {@code to}. */
    public double convert(double value, LengthUnit to) {
        return rescale(value, this, requireNonNull(to));
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
        from = requireNonNull(from);
        to = requireNonNull(to);
        if (from == to) {
            return value;
        }
        return to.fromInches(from.toInches(value));
    }

    /**
     * Convert a per-length quantity (PID P/I/D on a length or velocity error, centripetal scaling)
     * from {@code from} into {@code to}.
     */
    public static double rescaleInverse(double value, LengthUnit from, LengthUnit to) {
        from = requireNonNull(from);
        to = requireNonNull(to);
        if (from == to) {
            return value;
        }
        return value * from.unitsPerInch / to.unitsPerInch;
    }

    /**
     * Convert a length-squared or velocity-squared quantity (Kalman covariance on a length or
     * velocity error) from {@code from} into {@code to}.
     */
    public static double rescaleSquared(double value, LengthUnit from, LengthUnit to) {
        from = requireNonNull(from);
        to = requireNonNull(to);
        if (from == to) {
            return value;
        }
        double factor = to.unitsPerInch / from.unitsPerInch;
        return value * factor * factor;
    }

    public static LengthUnit requireNonNull(LengthUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("length unit must not be null");
        }
        return unit;
    }
}
