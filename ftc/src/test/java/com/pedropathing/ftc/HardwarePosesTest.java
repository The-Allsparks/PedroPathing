package com.pedropathing.ftc;

import static org.junit.Assert.assertEquals;

import com.pedropathing.geometry.Pose;
import com.pedropathing.math.LengthUnit;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.junit.Test;

public class HardwarePosesTest {
    private static final double EPS = 1e-12;

    @Test
    public void pinpointFeetPositionAndVelocityAreConsistent() {
        Pose position = HardwarePoses.toFollower(24, 12, 0.1, DistanceUnit.INCH, LengthUnit.FEET);
        Pose velocity = HardwarePoses.toFollower(36, -12, 0.2, DistanceUnit.INCH, LengthUnit.FEET);
        assertEquals(2.0, position.getX(), EPS);
        assertEquals(1.0, position.getY(), EPS);
        assertEquals(0.1, position.getHeading(), EPS);
        assertEquals(3.0, velocity.getX(), EPS);
        assertEquals(-1.0, velocity.getY(), EPS);
        double[] hardware = HardwarePoses.toHardware(position, LengthUnit.FEET, DistanceUnit.INCH, AngleUnit.RADIANS);
        assertEquals(24.0, hardware[0], EPS);
        assertEquals(12.0, hardware[1], EPS);
        assertEquals(0.1, hardware[2], EPS);
    }

    @Test
    public void otosFeetPositionAndVelocityAreConsistent() {
        Pose position = HardwarePoses.toFollower(12, 24, 90, DistanceUnit.INCH, LengthUnit.FEET, AngleUnit.DEGREES);
        Pose velocity = HardwarePoses.toFollower(12, 0, 0, DistanceUnit.INCH, LengthUnit.FEET, AngleUnit.DEGREES);
        assertEquals(1.0, position.getX(), EPS);
        assertEquals(2.0, position.getY(), EPS);
        assertEquals(Math.PI / 2, position.getHeading(), EPS);
        assertEquals(1.0, velocity.getX(), EPS);
        double[] hardware = HardwarePoses.toHardware(position, LengthUnit.FEET, DistanceUnit.INCH, AngleUnit.DEGREES);
        assertEquals(12.0, hardware[0], EPS);
        assertEquals(24.0, hardware[1], EPS);
        assertEquals(90.0, hardware[2], EPS);
    }

    @Test
    public void octoQuadFeetUsesMillimeterBoundary() {
        assertEquals(1.0, LengthUnit.FEET.fromMillimeters(304.8), EPS);
        assertEquals(304.8, LengthUnit.FEET.toMillimeters(1.0), EPS);
        Pose pose = new Pose(2, 0.5, 0);
        assertEquals(609.6, LengthUnit.FEET.toMillimeters(pose.getX()), EPS);
    }

    @Test
    public void hardwareOffsetsAreNotRescaledByFollowerUnit() {
        double pinpointOffsetInches = 1.5;
        assertEquals(1.5, pinpointOffsetInches, EPS);
        assertEquals(1.5, LengthUnits.toHardware(LengthUnits.toFollower(1.5, DistanceUnit.INCH, LengthUnit.FEET), LengthUnit.FEET, DistanceUnit.INCH), EPS);
    }
}
