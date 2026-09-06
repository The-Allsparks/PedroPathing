package com.pedropathing.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.pedropathing.geometry.Pose;

import org.junit.Test;

public class LengthDrawingTest {
    private static final double EPS = 1e-12;

    @Test
    public void convertsFollowerValuesToInchesOnce() {
        assertEquals(1.0, LengthDrawing.toInches(2.54, LengthUnit.CENTIMETERS), EPS);
        assertEquals(12.0, LengthDrawing.toInches(1.0, LengthUnit.FEET), EPS);
        assertEquals(5.0, LengthDrawing.toInches(5.0, LengthUnit.INCHES), EPS);
    }

    @Test
    public void convertsPoseToInchesWithoutChangingHeading() {
        Pose cm = new Pose(2.54, 5.08, 0.3);
        Pose inches = LengthDrawing.toInches(cm, LengthUnit.CENTIMETERS);
        assertEquals(1.0, inches.getX(), EPS);
        assertEquals(2.0, inches.getY(), EPS);
        assertEquals(0.3, inches.getHeading(), EPS);
        assertEquals(2.54, cm.getX(), EPS);
    }

    @Test
    public void inchPoseIsUnchanged() {
        Pose pose = new Pose(3, 4, 0);
        assertSame(pose, LengthDrawing.toInches(pose, LengthUnit.INCHES));
    }

    @Test
    public void nullPoseStaysNull() {
        assertNull(LengthDrawing.toInches((Pose) null, LengthUnit.FEET));
    }

    @Test
    public void pathPointsConvertInPlaceExactlyOnce() {
        double[][] points = new double[][] {{30.48, 60.96}, {0, 0}};
        LengthDrawing.pathPointsToInches(points, LengthUnit.CENTIMETERS);
        assertEquals(12.0, points[0][0], EPS);
        assertEquals(24.0, points[0][1], EPS);
        LengthDrawing.pathPointsToInches(points, LengthUnit.INCHES);
        assertEquals(12.0, points[0][0], EPS);
    }
}
