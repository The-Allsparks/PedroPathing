package com.pedropathing.ftc;

import com.pedropathing.math.LengthUnit;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Maps Pedro {@link LengthUnit} to FTC {@link DistanceUnit} for hardware localizers.
 *
 * @author The Allsparks - 36117
 */
public final class LengthUnits {
    private LengthUnits() {}

    public static DistanceUnit toFtc(LengthUnit unit) {
        if (unit == LengthUnit.CENTIMETERS) {
            return DistanceUnit.CM;
        }
        if (unit == LengthUnit.MILLIMETERS) {
            return DistanceUnit.MM;
        }
        if (unit == LengthUnit.METERS) {
            return DistanceUnit.METER;
        }
        return DistanceUnit.INCH;
    }

    public static LengthUnit fromFtc(DistanceUnit unit) {
        if (unit == DistanceUnit.CM) {
            return LengthUnit.CENTIMETERS;
        }
        if (unit == DistanceUnit.MM) {
            return LengthUnit.MILLIMETERS;
        }
        if (unit == DistanceUnit.METER) {
            return LengthUnit.METERS;
        }
        return LengthUnit.INCHES;
    }

    public static DistanceUnit activeFtc() {
        return toFtc(LengthUnit.active());
    }
}
