package com.pedropathing.ftc;

import static org.junit.Assert.assertEquals;

import com.pedropathing.geometry.PedroCoordinates;
import com.pedropathing.geometry.Pose;
import com.pedropathing.math.LengthUnit;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.junit.Test;

public class PoseConverterTest {
    private static final double EPS = 1e-9;

    @Test
    public void roundTripEveryFollowerUnitWithMatchingHardware() {
        DistanceUnit[] hardware = {DistanceUnit.INCH, DistanceUnit.CM, DistanceUnit.METER};
        LengthUnit[] followers = {LengthUnit.INCHES, LengthUnit.CENTIMETERS, LengthUnit.METERS};
        for (int i = 0; i < hardware.length; i++) {
            Pose original = new Pose(12, -8, 0.4, PedroCoordinates.INSTANCE);
            Pose2D hardwarePose = PoseConverter.poseToPose2D(original, PedroCoordinates.INSTANCE, followers[i], hardware[i]);
            Pose roundTrip = PoseConverter.pose2DToPose(hardwarePose, PedroCoordinates.INSTANCE, hardware[i], followers[i]);
            assertEquals(original.getX(), roundTrip.getX(), EPS);
            assertEquals(original.getY(), roundTrip.getY(), EPS);
            assertEquals(original.getHeading(), roundTrip.getHeading(), EPS);
        }
    }

    @Test
    public void feetFollowerWithInchHardwareRoundTrips() {
        Pose feet = new Pose(2, 0.5, 0.25, PedroCoordinates.INSTANCE);
        Pose2D hardware = PoseConverter.poseToPose2D(feet, PedroCoordinates.INSTANCE, LengthUnit.FEET, DistanceUnit.INCH);
        assertEquals(24.0, hardware.getX(DistanceUnit.INCH), EPS);
        assertEquals(6.0, hardware.getY(DistanceUnit.INCH), EPS);
        Pose roundTrip = PoseConverter.pose2DToPose(hardware, PedroCoordinates.INSTANCE, DistanceUnit.INCH, LengthUnit.FEET);
        assertEquals(2.0, roundTrip.getX(), EPS);
        assertEquals(0.5, roundTrip.getY(), EPS);
        assertEquals(0.25, roundTrip.getHeading(), EPS);
    }

    @Test
    public void coordinateConversionCombinesWithUnitConversion() {
        Pose pedroCm = new Pose(LengthUnit.CENTIMETERS.fieldCenter(), LengthUnit.CENTIMETERS.fieldCenter(), 0);
        Pose2D ftc = PoseConverter.poseToPose2D(
                pedroCm,
                FTCCoordinates.in(LengthUnit.CENTIMETERS),
                LengthUnit.CENTIMETERS,
                DistanceUnit.CM);
        assertEquals(0.0, ftc.getX(DistanceUnit.CM), 1e-6);
        assertEquals(0.0, ftc.getY(DistanceUnit.CM), 1e-6);
        assertEquals(-Math.PI / 2, ftc.getHeading(AngleUnit.RADIANS), EPS);
    }
}
