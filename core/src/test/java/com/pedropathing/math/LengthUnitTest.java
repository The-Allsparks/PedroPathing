package com.pedropathing.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

public class LengthUnitTest {
    private static final double EPS = 1e-12;
    private static final LengthUnit[] UNITS = LengthUnit.values();

    @Test
    public void selectableUnitsAreInchesCentimetersMetersAndFeet() {
        assertEquals(4, LengthUnit.values().length);
        LengthUnit.valueOf("INCHES");
        LengthUnit.valueOf("CENTIMETERS");
        LengthUnit.valueOf("METERS");
        LengthUnit.valueOf("FEET");
        try {
            LengthUnit.valueOf("MILLIMETERS");
            fail("millimeters must not be a selectable follower unit");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void everyPairRoundTrips() {
        for (LengthUnit from : UNITS) {
            for (LengthUnit to : UNITS) {
                double original = 12.0;
                double converted = from.convert(original, to);
                assertEquals(from.name() + " -> " + to.name(), original, to.convert(converted, from), EPS);
            }
        }
    }

    @Test
    public void identityConversions() {
        for (LengthUnit unit : UNITS) {
            assertEquals(7.5, unit.convert(7.5, unit), EPS);
            assertEquals(7.5, LengthUnit.rescale(7.5, unit, unit), EPS);
            assertEquals(7.5, LengthUnit.rescaleInverse(7.5, unit, unit), EPS);
            assertEquals(7.5, LengthUnit.rescaleSquared(7.5, unit, unit), EPS);
        }
    }

    @Test
    public void knownPhysicalEquivalents() {
        assertEquals(2.54, LengthUnit.INCHES.convert(1.0, LengthUnit.CENTIMETERS), EPS);
        assertEquals(0.0254, LengthUnit.INCHES.convert(1.0, LengthUnit.METERS), EPS);
        assertEquals(1.0, LengthUnit.INCHES.convert(12.0, LengthUnit.FEET), EPS);
        assertEquals(1.0, LengthUnit.FEET.fromInches(12.0), EPS);
        assertEquals(12.0, LengthUnit.FEET.toInches(1.0), EPS);
        assertEquals(30.48, LengthUnit.FEET.convert(1.0, LengthUnit.CENTIMETERS), EPS);
    }

    @Test
    public void tileFieldCenterAndMirrorHelpers() {
        assertEquals(24.0, LengthUnit.INCHES.tile(), EPS);
        assertEquals(60.96, LengthUnit.CENTIMETERS.tile(), EPS);
        assertEquals(2.0, LengthUnit.FEET.tile(), EPS);
        assertEquals(0.6096, LengthUnit.METERS.tile(), EPS);

        assertEquals(144.0, LengthUnit.INCHES.fieldSize(), EPS);
        assertEquals(72.0, LengthUnit.INCHES.fieldCenter(), EPS);
        assertEquals(182.88, LengthUnit.CENTIMETERS.fieldCenter(), EPS);
        assertEquals(6.0, LengthUnit.FEET.fieldCenter(), EPS);

        assertEquals(141.5, LengthUnit.INCHES.mirrorFieldLength(), EPS);
        assertEquals(141.5 * 2.54, LengthUnit.CENTIMETERS.mirrorFieldLength(), EPS);
        assertEquals(141.5 / 12.0, LengthUnit.FEET.mirrorFieldLength(), EPS);
    }

    @Test
    public void millimeterBoundaryHelpers() {
        assertEquals(25.4, LengthUnit.INCHES.toMillimeters(1.0), EPS);
        assertEquals(1.0, LengthUnit.INCHES.fromMillimeters(25.4), EPS);
        assertEquals(304.8, LengthUnit.FEET.toMillimeters(1.0), EPS);
        assertEquals(1.0, LengthUnit.FEET.fromMillimeters(304.8), EPS);
        assertEquals(10.0, LengthUnit.CENTIMETERS.toMillimeters(1.0), EPS);
        assertEquals(1000.0, LengthUnit.METERS.toMillimeters(1.0), EPS);
    }

    @Test
    public void rescaleLengthInverseAndSquared() {
        assertEquals(2.54, LengthUnit.rescale(1.0, LengthUnit.INCHES, LengthUnit.CENTIMETERS), EPS);
        assertEquals(0.1 / 2.54, LengthUnit.rescaleInverse(0.1, LengthUnit.INCHES, LengthUnit.CENTIMETERS), EPS);
        assertEquals(6.0 * 2.54 * 2.54, LengthUnit.rescaleSquared(6.0, LengthUnit.INCHES, LengthUnit.CENTIMETERS), EPS);
        assertEquals(12.0, LengthUnit.rescale(1.0, LengthUnit.FEET, LengthUnit.INCHES), EPS);
        assertEquals(1.2, LengthUnit.rescaleInverse(0.1, LengthUnit.INCHES, LengthUnit.FEET), EPS);
    }

    @Test
    public void nullUnitsAreRejected() {
        assertThrows(() -> LengthUnit.requireNonNull(null));
        assertThrows(() -> LengthUnit.INCHES.convert(1.0, null));
        assertThrows(() -> LengthUnit.rescale(1.0, null, LengthUnit.INCHES));
        assertThrows(() -> LengthUnit.rescale(1.0, LengthUnit.INCHES, null));
        assertThrows(() -> LengthUnit.rescaleInverse(1.0, null, LengthUnit.FEET));
        assertThrows(() -> LengthUnit.rescaleSquared(1.0, LengthUnit.METERS, null));
    }

    @Test
    public void classLoadingDoesNotSelectAUnit() {
        assertEquals(LengthUnit.INCHES, LengthContext.INCHES.unit());
        assertEquals(LengthUnit.CENTIMETERS, LengthContext.CENTIMETERS.unit());
        assertFalse(LengthContext.INCHES.equals(LengthContext.CENTIMETERS));
    }

    private static void assertThrows(Runnable runnable) {
        try {
            runnable.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail("expected IllegalArgumentException");
    }
}
