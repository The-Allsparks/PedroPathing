package com.pedropathing.ftc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.pedropathing.geometry.Pose;

import org.junit.Test;

public class FTCCoordinatesTest {
    private static final double EPS = 1e-9;

    @Test
    public void instanceRemainsInchBased() {
        Pose pedroCenter = new Pose(72, 72, 0);
        Pose ftc = FTCCoordinates.INSTANCE.convertFromPedro(pedroCenter);
        assertEquals(0.0, ftc.getX(), EPS);
        assertEquals(0.0, ftc.getY(), EPS);
        assertSame(FTCCoordinates.INSTANCE, ftc.getCoordinateSystem());
        Pose back = FTCCoordinates.INSTANCE.convertToPedro(ftc);
        assertEquals(72.0, back.getX(), EPS);
        assertEquals(72.0, back.getY(), EPS);
    }

    @Test
    public void invertedCoordinatesRemainInchBased() {
        Pose pedroCenter = new Pose(72, 72, 0);
        Pose inverted = InvertedFTCCoordinates.INSTANCE.convertFromPedro(pedroCenter);
        assertEquals(0.0, inverted.getX(), EPS);
        assertEquals(0.0, inverted.getY(), EPS);
    }
}
