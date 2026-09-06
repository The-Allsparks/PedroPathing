package com.pedropathing.ftc;

import com.pedropathing.geometry.CoordinateSystem;
import com.pedropathing.geometry.Pose;
import com.pedropathing.math.LengthUnit;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class PoseConverter {

    /**
     * Converts a Pedro {@link Pose} to an FTC {@link Pose2D}.
     *
     * @param pose the Pose object, with x/y in {@code followerUnit}
     * @param desiredCoordinateSystem the desired coordinate system
     * @param followerUnit the unit of the Pedro pose
     * @param hardwareUnit the unit stored in the returned Pose2D
     */
    public static Pose2D poseToPose2D(
            Pose pose,
            CoordinateSystem desiredCoordinateSystem,
            LengthUnit followerUnit,
            DistanceUnit hardwareUnit) {
        Pose converted = pose.getAsCoordinateSystem(desiredCoordinateSystem);
        double x = LengthUnits.toHardware(converted.getX(), followerUnit, hardwareUnit);
        double y = LengthUnits.toHardware(converted.getY(), followerUnit, hardwareUnit);
        return new Pose2D(hardwareUnit, x, y, AngleUnit.RADIANS, converted.getHeading());
    }

    /**
     * Converts an FTC {@link Pose2D} to a Pedro {@link Pose}.
     *
     * @param pose2d the Pose2D object
     * @param coordinateSystem the coordinate system
     * @param hardwareUnit the unit to read from {@code pose2d}
     * @param followerUnit the unit of the returned Pedro pose
     */
    public static Pose pose2DToPose(
            Pose2D pose2d,
            CoordinateSystem coordinateSystem,
            DistanceUnit hardwareUnit,
            LengthUnit followerUnit) {
        double x = LengthUnits.toFollower(pose2d.getX(hardwareUnit), hardwareUnit, followerUnit);
        double y = LengthUnits.toFollower(pose2d.getY(hardwareUnit), hardwareUnit, followerUnit);
        return new Pose(x, y, pose2d.getHeading(AngleUnit.RADIANS), coordinateSystem);
    }
}
