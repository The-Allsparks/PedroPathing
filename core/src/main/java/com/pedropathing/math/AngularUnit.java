package com.pedropathing.math;

/**
 * User-interface angle unit for Pedro Pathing.
 *
 * <p>Named {@code AngularUnit} to avoid colliding with the FTC SDK {@code AngleUnit} type. Pedro
 * always calculates internally in radians. This enum is used by {@link PedroUnits} to convert
 * TeamCode-facing angles into radians on input and back out of radians on output.
 *
 * @author The Allsparks - 36117
 */
public enum AngularUnit {
    RADIANS("rad", "radians"),
    DEGREES("deg", "degrees");

    private final String symbol;
    private final String pluralName;

    AngularUnit(String symbol, String pluralName) {
        this.symbol = symbol;
        this.pluralName = pluralName;
    }

    public String symbol() {
        return symbol;
    }

    public String pluralName() {
        return pluralName;
    }

    /** Convert a value in radians into this unit. */
    public double fromRadians(double radians) {
        return this == DEGREES ? Math.toDegrees(radians) : radians;
    }

    /** Convert a value in this unit into radians. */
    public double toRadians(double value) {
        return this == DEGREES ? Math.toRadians(value) : value;
    }

    public static AngularUnit requireNonNull(AngularUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("angle unit must not be null");
        }
        return unit;
    }
}
