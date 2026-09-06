package com.pedropathing.ftc;

import com.pedropathing.math.LengthUnit;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Converts between Pedro's canonical inches and an FTC {@link DistanceUnit} at a hardware boundary.
 *
 * <p>User-facing {@link com.pedropathing.math.PedroUnits} never enter this class. Feet are converted
 * to inches before a pose reaches hardware code.
 *
 * @author The Allsparks - 36117
 */
public final class HardwareLengths {
    private HardwareLengths() {}

    /** Convert a hardware length into canonical Pedro inches. */
    public static double toInches(double value, DistanceUnit hardwareUnit) {
        requireHardwareUnit(hardwareUnit);
        return DistanceUnit.INCH.fromUnit(hardwareUnit, value);
    }

    /** Convert canonical Pedro inches into the configured hardware unit. */
    public static double fromInches(double inches, DistanceUnit hardwareUnit) {
        requireHardwareUnit(hardwareUnit);
        return hardwareUnit.fromUnit(DistanceUnit.INCH, inches);
    }

    /** Convert hardware millimeters into canonical Pedro inches. */
    public static double millimetersToInches(double millimeters) {
        return LengthUnit.millimetersToInches(millimeters);
    }

    /** Convert canonical Pedro inches into hardware millimeters. */
    public static double inchesToMillimeters(double inches) {
        return LengthUnit.inchesToMillimeters(inches);
    }

    public static DistanceUnit requireHardwareUnit(DistanceUnit hardwareUnit) {
        if (hardwareUnit == null) {
            throw new IllegalArgumentException("hardware distance unit must not be null");
        }
        return hardwareUnit;
    }
}
