package com.pedropathing.ftc;

import static org.junit.Assert.assertEquals;

import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.junit.Test;

public class HardwarePosesTest {
    private static final double EPS = 1e-9;

    @Test
    public void pinpointMillimetersBecomeCanonicalInches() {
        Pose pose = HardwarePoses.toInternal(304.8, 152.4, Math.PI / 2, DistanceUnit.MM);
        assertEquals(12.0, pose.getX(), EPS);
        assertEquals(6.0, pose.getY(), EPS);
        assertEquals(Math.PI / 2, pose.getHeading(), EPS);
    }

    @Test
    public void otosCentimetersBecomeCanonicalInchesIncludingAccelerationScale() {
        Pose pose = HardwarePoses.toInternal(2.54, 5.08, 0, DistanceUnit.CM);
        assertEquals(1.0, pose.getX(), EPS);
        assertEquals(2.0, pose.getY(), EPS);
        double inchesPerSecondSquared = HardwareLengths.toInches(2.54, DistanceUnit.CM);
        assertEquals(1.0, inchesPerSecondSquared, EPS);
    }

    @Test
    public void setPoseInchesConvertToHardwareMillimeters() {
        Pose internal = new Pose(12, 6, 0);
        double[] hardware = HardwarePoses.toHardware(internal, DistanceUnit.MM, AngleUnit.RADIANS);
        assertEquals(304.8, hardware[0], EPS);
        assertEquals(152.4, hardware[1], EPS);
        assertEquals(0.0, hardware[2], EPS);
    }

    @Test
    public void feetInterfaceIsConvertedBeforeHardwareBoundary() {
        double inches = 2.0 * 12.0;
        double[] hardware = HardwarePoses.toHardware(new Pose(inches, 0, 0), DistanceUnit.INCH, AngleUnit.RADIANS);
        assertEquals(24.0, hardware[0], EPS);
    }

    @Test
    public void roundTripDoesNotDoubleConvert() {
        Pose original = new Pose(18, -4, 0.3);
        double[] hardware = HardwarePoses.toHardware(original, DistanceUnit.CM, AngleUnit.RADIANS);
        Pose roundTrip = HardwarePoses.toInternal(hardware[0], hardware[1], hardware[2], DistanceUnit.CM, AngleUnit.RADIANS);
        assertEquals(original.getX(), roundTrip.getX(), EPS);
        assertEquals(original.getY(), roundTrip.getY(), EPS);
        assertEquals(original.getHeading(), roundTrip.getHeading(), EPS);
    }
}
