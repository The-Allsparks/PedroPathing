package com.pedropathing.ftc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.pedropathing.math.LengthUnit;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.junit.Test;

public class LengthUnitsTest {
    private static final double EPS = 1e-12;

    @Test
    public void feetFollowerWithInchHardware() {
        assertEquals(2.0, LengthUnits.toFollower(24.0, DistanceUnit.INCH, LengthUnit.FEET), EPS);
        assertEquals(24.0, LengthUnits.toHardware(2.0, LengthUnit.FEET, DistanceUnit.INCH), EPS);
    }

    @Test
    public void centimeterFollowerWithInchHardware() {
        assertEquals(2.54, LengthUnits.toFollower(1.0, DistanceUnit.INCH, LengthUnit.CENTIMETERS), EPS);
        assertEquals(1.0, LengthUnits.toHardware(2.54, LengthUnit.CENTIMETERS, DistanceUnit.INCH), EPS);
    }

    @Test
    public void millimeterHardwareIsBoundaryOnly() {
        assertEquals(1.0, LengthUnits.toFollower(25.4, DistanceUnit.MM, LengthUnit.INCHES), EPS);
        assertEquals(1.0 / 12.0, LengthUnits.toFollower(25.4, DistanceUnit.MM, LengthUnit.FEET), EPS);
        assertEquals(25.4, LengthUnits.toHardware(1.0, LengthUnit.INCHES, DistanceUnit.MM), EPS);
        assertEquals(304.8, LengthUnits.toHardware(1.0, LengthUnit.FEET, DistanceUnit.MM), EPS);
    }

    @Test
    public void meterAndCentimeterHardware() {
        assertEquals(1.0, LengthUnits.toFollower(0.0254, DistanceUnit.METER, LengthUnit.INCHES), EPS);
        assertEquals(1.0, LengthUnits.toFollower(2.54, DistanceUnit.CM, LengthUnit.INCHES), EPS);
        assertEquals(0.0254, LengthUnits.toHardware(1.0, LengthUnit.INCHES, DistanceUnit.METER), EPS);
    }

    @Test
    public void toFtcDoesNotPretendFeetAreInchesForTheFollower() {
        assertEquals(DistanceUnit.INCH, LengthUnits.toFtc(LengthUnit.FEET));
        assertEquals(DistanceUnit.INCH, LengthUnits.toFtc(LengthUnit.INCHES));
        assertEquals(DistanceUnit.CM, LengthUnits.toFtc(LengthUnit.CENTIMETERS));
        assertEquals(DistanceUnit.METER, LengthUnits.toFtc(LengthUnit.METERS));
        assertEquals(2.0, LengthUnits.toFollower(24.0, LengthUnits.toFtc(LengthUnit.FEET), LengthUnit.FEET), EPS);
    }

    @Test
    public void nullUnitsAreRejected() {
        try {
            LengthUnits.toFollower(1.0, null, LengthUnit.INCHES);
            fail();
        } catch (IllegalArgumentException expected) {
            // expected
        }
        try {
            LengthUnits.toHardware(1.0, null, DistanceUnit.INCH);
            fail();
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }
}
