package com.pedropathing.math;

/**
 * User-interface mass unit for Pedro Pathing.
 *
 * <p>Pedro always calculates internally in kilograms. This enum is used by {@link PedroUnits} to
 * convert TeamCode-facing mass values into kilograms on input and back out of kilograms on output.
 *
 * @author The Allsparks - 36117
 */
public enum MassUnit {
    KILOGRAMS("kg", "kilograms", 1.0),
    POUNDS("lb", "pounds", 1.0 / 0.45359237);

    /** International avoirdupois pound. */
    public static final double KILOGRAMS_PER_POUND = 0.45359237;

    private final String symbol;
    private final String pluralName;
    private final double unitsPerKilogram;

    MassUnit(String symbol, String pluralName, double unitsPerKilogram) {
        this.symbol = symbol;
        this.pluralName = pluralName;
        this.unitsPerKilogram = unitsPerKilogram;
    }

    public String symbol() {
        return symbol;
    }

    public String pluralName() {
        return pluralName;
    }

    /** Convert a value in kilograms into this unit. */
    public double fromKilograms(double kilograms) {
        return kilograms * unitsPerKilogram;
    }

    /** Convert a value in this unit into kilograms. */
    public double toKilograms(double value) {
        return value / unitsPerKilogram;
    }

    public static MassUnit requireNonNull(MassUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("mass unit must not be null");
        }
        return unit;
    }
}
