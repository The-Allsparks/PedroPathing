package com.pedropathing.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LengthUnitTest {
    private static final double EPS = 1e-12;

    @Test
    public void centimetersRoundTripThroughInches() {
        assertEquals(1.0, LengthUnit.CENTIMETERS.toInches(2.54), EPS);
        assertEquals(2.54, LengthUnit.INCHES.convert(1.0, LengthUnit.CENTIMETERS), EPS);
    }

    @Test
    public void tileIsTwentyFourInches() {
        assertEquals(24.0, LengthUnit.INCHES.tile(), EPS);
        assertEquals(60.96, LengthUnit.CENTIMETERS.tile(), EPS);
    }

    @Test
    public void fieldCenterIsHalfField() {
        assertEquals(72.0, LengthUnit.INCHES.fieldCenter(), EPS);
        assertEquals(182.88, LengthUnit.CENTIMETERS.fieldCenter(), EPS);
    }

    @Test
    public void millimetersMatchInches() {
        assertEquals(25.4, LengthUnit.INCHES.toMillimeters(1.0), EPS);
        assertEquals(1.0, LengthUnit.MILLIMETERS.fromMillimeters(1.0), EPS);
    }

    @Test
    public void setActiveRejectsNull() {
        LengthUnit previous = LengthUnit.active();
        try {
            LengthUnit.setActive(LengthUnit.CENTIMETERS);
            assertEquals(LengthUnit.CENTIMETERS, LengthUnit.active());
            try {
                LengthUnit.setActive(null);
            } catch (IllegalArgumentException expected) {
                assertEquals(LengthUnit.CENTIMETERS, LengthUnit.active());
                return;
            }
            throw new AssertionError("expected IllegalArgumentException");
        } finally {
            LengthUnit.setActive(previous);
        }
    }
}
