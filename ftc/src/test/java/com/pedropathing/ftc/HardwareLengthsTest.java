package com.pedropathing.ftc;

import static org.junit.Assert.assertEquals;

import com.pedropathing.math.LengthUnit;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.junit.Test;

public class HardwareLengthsTest {
    private static final double EPS = 1e-9;

    @Test
    public void hardwareUnitsConvertToCanonicalInches() {
        assertEquals(1.0, HardwareLengths.toInches(1.0, DistanceUnit.INCH), EPS);
        assertEquals(1.0, HardwareLengths.toInches(2.54, DistanceUnit.CM), EPS);
        assertEquals(1.0, HardwareLengths.toInches(0.0254, DistanceUnit.METER), EPS);
        assertEquals(1.0, HardwareLengths.toInches(25.4, DistanceUnit.MM), EPS);
    }

    @Test
    public void canonicalInchesConvertToHardwareUnits() {
        assertEquals(1.0, HardwareLengths.fromInches(1.0, DistanceUnit.INCH), EPS);
        assertEquals(2.54, HardwareLengths.fromInches(1.0, DistanceUnit.CM), EPS);
        assertEquals(0.0254, HardwareLengths.fromInches(1.0, DistanceUnit.METER), EPS);
        assertEquals(25.4, HardwareLengths.fromInches(1.0, DistanceUnit.MM), EPS);
    }

    @Test
    public void feetInterfaceDoesNotBelongHere() {
        assertEquals(24.0, HardwareLengths.toInches(24.0, DistanceUnit.INCH), EPS);
        assertEquals(304.8, HardwareLengths.fromInches(12.0, DistanceUnit.MM), EPS);
    }

    @Test
    public void millimeterHelpers() {
        assertEquals(1.0, HardwareLengths.millimetersToInches(25.4), EPS);
        assertEquals(25.4, HardwareLengths.inchesToMillimeters(1.0), EPS);
    }

    @Test
    public void teamCodeLengthMapsToHardwareDistanceUnit() {
        assertEquals(DistanceUnit.INCH, HardwareLengths.toDistanceUnit(LengthUnit.INCHES));
        assertEquals(DistanceUnit.INCH, HardwareLengths.toDistanceUnit(LengthUnit.FEET));
        assertEquals(DistanceUnit.CM, HardwareLengths.toDistanceUnit(LengthUnit.CENTIMETERS));
        assertEquals(DistanceUnit.METER, HardwareLengths.toDistanceUnit(LengthUnit.METERS));
    }
}
