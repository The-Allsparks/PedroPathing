package com.pedropathing.math;

/**
 * User-interface length unit for Pedro Pathing.
 *
 * <p>Pedro always calculates internally in inches. This enum is used by {@link PedroUnits} to
 * convert TeamCode-facing values into inches on input and back out of inches on output.
 *
 * <p>Millimeters are not a selectable interface unit. Hardware that reports millimeters (OctoQuad)
 * should use {@link #inchesToMillimeters(double)} and {@link #millimetersToInches(double)} at the
 * adapter boundary.
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

    /** Convert canonical inches into millimeters at a hardware boundary. */
    public static double inchesToMillimeters(double inches) {
        return inches * MILLIMETERS_PER_INCH;
    }

    /** Convert hardware millimeters into canonical inches. */
    public static double millimetersToInches(double millimeters) {
        return millimeters / MILLIMETERS_PER_INCH;
    }

    public static LengthUnit requireNonNull(LengthUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("length unit must not be null");
        }
        return unit;
    }
}
