package com.pedropathing.ftc;

import com.pedropathing.math.LengthUnit;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Converts between Pedro follower {@link LengthUnit} values and FTC {@link DistanceUnit} hardware
 * measurements. Conversion happens once at the hardware adapter boundary.
 *
 * <p>FTC {@link DistanceUnit} has no foot. A feet follower still uses an independent hardware unit
 * (typically inches) and converts explicitly. Millimeters may appear as a hardware unit (OctoQuad,
 * {@link DistanceUnit#MM}) but are not a selectable follower unit.
 *
 * @author The Allsparks - 36117
 */
public final class LengthUnits {
    private LengthUnits() {}

    /**
     * Convert a hardware length into the follower unit.
     */
    public static double toFollower(double value, DistanceUnit hardwareUnit, LengthUnit followerUnit) {
        requireHardwareUnit(hardwareUnit);
        LengthUnit.requireNonNull(followerUnit);
        double inches = DistanceUnit.INCH.fromUnit(hardwareUnit, value);
        return followerUnit.fromInches(inches);
    }

    /**
     * Convert a follower length into the configured hardware unit.
     */
    public static double toHardware(double value, LengthUnit followerUnit, DistanceUnit hardwareUnit) {
        LengthUnit.requireNonNull(followerUnit);
        requireHardwareUnit(hardwareUnit);
        double inches = followerUnit.toInches(value);
        return hardwareUnit.fromUnit(DistanceUnit.INCH, inches);
    }

    /**
     * Map a follower unit onto an FTC {@link DistanceUnit} when the hardware should speak the
     * same physical unit. {@link LengthUnit#FEET} has no FTC equivalent and returns
     * {@link DistanceUnit#INCH} so the adapter can convert independently.
     */
    public static DistanceUnit toFtc(LengthUnit unit) {
        LengthUnit.requireNonNull(unit);
        if (unit == LengthUnit.CENTIMETERS) {
            return DistanceUnit.CM;
        }
        if (unit == LengthUnit.METERS) {
            return DistanceUnit.METER;
        }
        return DistanceUnit.INCH;
    }

    public static DistanceUnit requireHardwareUnit(DistanceUnit hardwareUnit) {
        if (hardwareUnit == null) {
            throw new IllegalArgumentException("hardware distance unit must not be null");
        }
        return hardwareUnit;
    }
}
