package com.pedropathing.geometry;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PoseCanonicalTest {
    private static final double EPS = 1e-12;

    @Test
    public void constructorsRemainInchesAndRadians() {
        Pose pose = new Pose(24, 48, Math.PI / 2);
        assertEquals(24.0, pose.getX(), EPS);
        assertEquals(48.0, pose.getY(), EPS);
        assertEquals(Math.PI / 2, pose.getHeading(), EPS);
    }

    @Test
    public void mirrorWithoutArgumentsKeepsUpstreamInchFieldLength() {
        Pose pose = new Pose(10, 20, 0);
        Pose mirrored = pose.mirror();
        assertEquals(141.5 - 10, mirrored.getX(), EPS);
        assertEquals(20.0, mirrored.getY(), EPS);
    }

    @Test
    public void mirrorWithExplicitFieldLength() {
        Pose pose = new Pose(2, 0, 0);
        assertEquals(10.0, pose.mirror(12).getX(), EPS);
    }
}
