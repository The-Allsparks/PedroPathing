package com.pedropathing.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class LengthUnitTest {
    private static final double EPS = 1e-12;

    @Test
    public void selectableUnitsAreInchesFeetCentimetersAndMeters() {
        assertEquals(4, LengthUnit.values().length);
        LengthUnit.valueOf("INCHES");
        LengthUnit.valueOf("FEET");
        LengthUnit.valueOf("CENTIMETERS");
        LengthUnit.valueOf("METERS");
        try {
            LengthUnit.valueOf("MILLIMETERS");
            fail("millimeters must not be a selectable interface unit");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void knownPhysicalEquivalents() {
        assertEquals(2.54, LengthUnit.INCHES.convert(1.0, LengthUnit.CENTIMETERS), EPS);
        assertEquals(0.0254, LengthUnit.INCHES.convert(1.0, LengthUnit.METERS), EPS);
        assertEquals(1.0, LengthUnit.INCHES.convert(12.0, LengthUnit.FEET), EPS);
        assertEquals(12.0, LengthUnit.FEET.toInches(1.0), EPS);
        assertEquals(1.0, LengthUnit.CENTIMETERS.toInches(2.54), EPS);
    }

    @Test
    public void identityConversion() {
        for (LengthUnit unit : LengthUnit.values()) {
            assertEquals(7.5, unit.convert(7.5, unit), EPS);
            assertEquals(7.5, LengthUnit.rescale(7.5, unit, unit), EPS);
        }
    }

    @Test
    public void tileFieldAndMirrorHelpers() {
        assertEquals(24.0, LengthUnit.INCHES.tile(), EPS);
        assertEquals(2.0, LengthUnit.FEET.tile(), EPS);
        assertEquals(60.96, LengthUnit.CENTIMETERS.tile(), EPS);
        assertEquals(72.0, LengthUnit.INCHES.fieldCenter(), EPS);
        assertEquals(141.5, LengthUnit.INCHES.mirrorFieldLength(), EPS);
    }

    @Test
    public void millimeterHelpersAreHardwareOnly() {
        assertEquals(25.4, LengthUnit.inchesToMillimeters(1.0), EPS);
        assertEquals(1.0, LengthUnit.millimetersToInches(25.4), EPS);
        assertEquals(304.8, LengthUnit.inchesToMillimeters(12.0), EPS);
    }

    @Test
    public void nullUnitsAreRejected() {
        try {
            LengthUnit.requireNonNull(null);
            fail();
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }
}
