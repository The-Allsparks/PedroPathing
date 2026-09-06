package com.pedropathing.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import com.pedropathing.geometry.Pose;

import org.junit.Test;

public class LengthContextTest {
    private static final double EPS = 1e-12;

    @Test
    public void ofInternsPerUnit() {
        assertSame(LengthContext.CENTIMETERS, LengthContext.of(LengthUnit.CENTIMETERS));
        assertSame(LengthContext.FEET, LengthContext.of(LengthUnit.FEET));
    }

    @Test
    public void twoContextsDoNotAffectEachOther() {
        LengthContext inches = LengthContext.of(LengthUnit.INCHES);
        LengthContext centimeters = LengthContext.of(LengthUnit.CENTIMETERS);
        assertEquals(24.0, inches.tile(), EPS);
        assertEquals(60.96, centimeters.tile(), EPS);
        assertEquals(24.0, inches.tile(), EPS);
    }

    @Test
    public void constructionOrderDoesNotChangeValues() {
        LengthContext meters = LengthContext.of(LengthUnit.METERS);
        LengthContext feet = LengthContext.of(LengthUnit.FEET);
        LengthContext inches = LengthContext.of(LengthUnit.INCHES);
        assertEquals(0.6096, meters.tile(), EPS);
        assertEquals(2.0, feet.tile(), EPS);
        assertEquals(24.0, inches.tile(), EPS);
    }

    @Test
    public void convertPoseUsesExplicitSourceUnit() {
        Pose inchPose = new Pose(12, 24, 0.5);
        Pose feet = LengthContext.FEET.convertPose(inchPose, LengthUnit.INCHES);
        assertEquals(1.0, feet.getX(), EPS);
        assertEquals(2.0, feet.getY(), EPS);
        assertEquals(0.5, feet.getHeading(), EPS);
        assertEquals(12.0, inchPose.getX(), EPS);
    }

    @Test
    public void ofRejectsNull() {
        try {
            LengthContext.of(null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }
}
