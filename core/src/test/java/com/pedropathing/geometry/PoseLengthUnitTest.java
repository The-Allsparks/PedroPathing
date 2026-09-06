package com.pedropathing.geometry;

import static org.junit.Assert.assertEquals;

import com.pedropathing.math.LengthUnit;

import org.junit.Test;

public class PoseLengthUnitTest {
    private static final double EPS = 1e-12;

    @Test
    public void mirrorWithoutArgumentsKeepsUpstreamInchFieldLength() {
        Pose pose = new Pose(10, 20, 0);
        Pose mirrored = pose.mirror();
        assertEquals(141.5 - 10, mirrored.getX(), EPS);
        assertEquals(20.0, mirrored.getY(), EPS);
    }

    @Test
    public void mirrorWithUnitUsesThatUnitsFieldLength() {
        Pose pose = new Pose(10, 20, 0);
        Pose mirrored = pose.mirror(LengthUnit.CENTIMETERS);
        assertEquals(LengthUnit.CENTIMETERS.mirrorFieldLength() - 10, mirrored.getX(), EPS);
    }

    @Test
    public void mirrorWithExplicitFieldLength() {
        Pose pose = new Pose(2, 0, 0);
        assertEquals(10.0, pose.mirror(12).getX(), EPS);
    }
}
