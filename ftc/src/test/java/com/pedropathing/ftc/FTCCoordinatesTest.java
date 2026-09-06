package com.pedropathing.ftc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.pedropathing.geometry.Pose;
import com.pedropathing.math.LengthUnit;

import org.junit.Test;

public class FTCCoordinatesTest {
    private static final double EPS = 1e-9;

    @Test
    public void instanceRemainsInchBased() {
        assertSame(FTCCoordinates.in(LengthUnit.INCHES), FTCCoordinates.INSTANCE);
        Pose pedroCenter = new Pose(72, 72, 0);
        Pose ftc = FTCCoordinates.INSTANCE.convertFromPedro(pedroCenter);
        assertEquals(0.0, ftc.getX(), EPS);
        assertEquals(0.0, ftc.getY(), EPS);
    }

    @Test
    public void centimeterCoordinatesUseCentimeterFieldCenter() {
        double center = LengthUnit.CENTIMETERS.fieldCenter();
        Pose pedroCenter = new Pose(center, center, 0);
        Pose ftc = FTCCoordinates.in(LengthUnit.CENTIMETERS).convertFromPedro(pedroCenter);
        assertEquals(0.0, ftc.getX(), EPS);
        assertEquals(0.0, ftc.getY(), EPS);
        Pose back = FTCCoordinates.in(LengthUnit.CENTIMETERS).convertToPedro(ftc);
        assertEquals(center, back.getX(), EPS);
        assertEquals(center, back.getY(), EPS);
    }

    @Test
    public void invertedCoordinatesScaleWithUnit() {
        double center = LengthUnit.FEET.fieldCenter();
        Pose pedroCenter = new Pose(center, center, 0);
        Pose inverted = InvertedFTCCoordinates.in(LengthUnit.FEET).convertFromPedro(pedroCenter);
        assertEquals(0.0, inverted.getX(), EPS);
        assertEquals(0.0, inverted.getY(), EPS);
    }
}
