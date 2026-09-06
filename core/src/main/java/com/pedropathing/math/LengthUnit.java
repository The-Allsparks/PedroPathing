package com.pedropathing.math;

/**
 * Selectable length unit for Pedro Pathing poses, paths, localizer constants, and tuners.
 *
 * <p>The follower math is unit-agnostic: pick one unit and use it everywhere. Inches remain the
 * default so upstream Quickstart projects keep their existing numbers. Hardware localizers that
 * talk to FTC {@code DistanceUnit} are mapped from this value in the FTC adapter.
 *
 * <p>{@link #setActive(LengthUnit)} is called when a {@code FollowerBuilder} or {@code Follower}
 * is constructed so pose mirroring and dashboard drawing can convert without threading the unit
 * through every call site.
 *
 * @author The Allsparks - 36117
 */
public enum LengthUnit {
    INCHES("in", "inches", 1.0),
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

    public static LengthUnit active() {
        return active;
    }

    public static void setActive(LengthUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("length unit must not be null");
        }
        active = unit;
    }
}
