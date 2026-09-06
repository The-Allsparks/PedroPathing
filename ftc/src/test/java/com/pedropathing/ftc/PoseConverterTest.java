package com.pedropathing.ftc;

import static org.junit.Assert.assertEquals;

import com.pedropathing.geometry.PedroCoordinates;
import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.junit.Test;

public class PoseConverterTest {
    private static final double EPS = 1e-9;

    @Test
    public void twoArgOverloadsStayInchBased() {
        Pose original = new Pose(12, -8, 0.4, PedroCoordinates.INSTANCE);
        Pose2D pose2D = PoseConverter.poseToPose2D(original, PedroCoordinates.INSTANCE);
        assertEquals(12.0, pose2D.getX(DistanceUnit.INCH), EPS);
        assertEquals(-8.0, pose2D.getY(DistanceUnit.INCH), EPS);
        Pose roundTrip = PoseConverter.pose2DToPose(pose2D, PedroCoordinates.INSTANCE);
        assertEquals(original.getX(), roundTrip.getX(), EPS);
        assertEquals(original.getY(), roundTrip.getY(), EPS);
        assertEquals(original.getHeading(), roundTrip.getHeading(), EPS);
    }

    @Test
    public void roundTripEveryHardwareDistanceUnit() {
        DistanceUnit[] hardware = {DistanceUnit.INCH, DistanceUnit.CM, DistanceUnit.METER, DistanceUnit.MM};
        Pose original = new Pose(12, -8, 0.4, PedroCoordinates.INSTANCE);
        for (DistanceUnit unit : hardware) {
            Pose2D hardwarePose = PoseConverter.poseToPose2D(original, PedroCoordinates.INSTANCE, unit);
            Pose roundTrip = PoseConverter.pose2DToPose(hardwarePose, PedroCoordinates.INSTANCE, unit);
            assertEquals(unit.toString(), original.getX(), roundTrip.getX(), EPS);
            assertEquals(unit.toString(), original.getY(), roundTrip.getY(), EPS);
            assertEquals(unit.toString(), original.getHeading(), roundTrip.getHeading(), EPS);
        }
    }

    @Test
    public void millimeterHardwareDoesNotTreatPoseAsFeet() {
        Pose inches = new Pose(24, 6, 0.25, PedroCoordinates.INSTANCE);
        Pose2D hardware = PoseConverter.poseToPose2D(inches, PedroCoordinates.INSTANCE, DistanceUnit.MM);
        assertEquals(609.6, hardware.getX(DistanceUnit.MM), EPS);
        assertEquals(152.4, hardware.getY(DistanceUnit.MM), EPS);
        Pose roundTrip = PoseConverter.pose2DToPose(hardware, PedroCoordinates.INSTANCE, DistanceUnit.MM);
        assertEquals(24.0, roundTrip.getX(), EPS);
        assertEquals(6.0, roundTrip.getY(), EPS);
    }

    @Test
    public void ftcCoordinatesUseInchFieldCenter() {
        Pose pedroCenter = new Pose(72, 72, 0);
        Pose2D ftc = PoseConverter.poseToPose2D(pedroCenter, FTCCoordinates.INSTANCE, DistanceUnit.INCH);
        assertEquals(0.0, ftc.getX(DistanceUnit.INCH), 1e-6);
        assertEquals(0.0, ftc.getY(DistanceUnit.INCH), 1e-6);
        assertEquals(-Math.PI / 2, ftc.getHeading(AngleUnit.RADIANS), EPS);
    }
}
