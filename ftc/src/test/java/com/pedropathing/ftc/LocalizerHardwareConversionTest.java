package com.pedropathing.ftc;

import static org.junit.Assert.assertEquals;

import com.pedropathing.geometry.Pose;
import com.pedropathing.math.LengthUnit;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.junit.Test;

/**
 * Hardware-boundary conversions for Pinpoint, OTOS, and OctoQuad. Interface units such as feet
 * are converted to inches before these adapters run.
 */
public class LocalizerHardwareConversionTest {
    private static final double EPS = 1e-9;

    @Test
    public void pinpointPositionAndVelocityUseHardwareUnitIndependently() {
        Pose inches = HardwarePoses.toInternal(609.6, 304.8, 0, DistanceUnit.MM);
        assertEquals(24.0, inches.getX(), EPS);
        assertEquals(12.0, inches.getY(), EPS);
        assertEquals(2.0, HardwareLengths.toInches(50.8, DistanceUnit.MM), EPS);
        double[] hardware = HardwarePoses.toHardware(new Pose(24, 12, 0), DistanceUnit.MM, org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.RADIANS);
        assertEquals(609.6, hardware[0], EPS);
        assertEquals(304.8, hardware[1], EPS);
    }

    @Test
    public void otosInchesHardwareStaysInchesInternally() {
        Pose pose = HardwarePoses.toInternal(24, 6, 0.5, DistanceUnit.INCH);
        assertEquals(24.0, pose.getX(), EPS);
        assertEquals(6.0, pose.getY(), EPS);
        assertEquals(0.5, pose.getHeading(), EPS);
        assertEquals(9.81, HardwareLengths.toInches(9.81, DistanceUnit.INCH), EPS);
    }

    @Test
    public void octoQuadMillimetersConvertToInchesExactlyOnce() {
        assertEquals(12.0, LengthUnit.millimetersToInches(304.8), EPS);
        assertEquals(6.0, LengthUnit.millimetersToInches(152.4), EPS);
        assertEquals(2.0, LengthUnit.millimetersToInches(50.8), EPS);
        assertEquals(304.8, LengthUnit.inchesToMillimeters(12.0), EPS);
    }

    @Test
    public void feetInterfaceConvertedBeforeHardwareDoesNotCreatePrototypeError() {
        double canonicalInches = LengthUnit.FEET.toInches(2.0);
        assertEquals(24.0, canonicalInches, EPS);
        assertEquals(609.6, LengthUnit.inchesToMillimeters(canonicalInches), EPS);
        assertEquals(24.0, HardwareLengths.fromInches(canonicalInches, DistanceUnit.INCH), EPS);
    }
}
