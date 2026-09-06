package com.pedropathing.ftc;

import com.pedropathing.geometry.CoordinateSystem;
import com.pedropathing.geometry.Pose;
import com.pedropathing.math.LengthUnit;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class PoseConverter {

    /**
     * Converts a Pose to a Pose2D in the desired coordinate system using the active length unit.
     *
     * @param pose the Pose object
     * @param desiredCoordinateSystem the desired coordinate system
     * @return a Pose2D object with x/y in the active follower length unit and heading in radians
     */
    public static Pose2D poseToPose2D(Pose pose, CoordinateSystem desiredCoordinateSystem) {
        return poseToPose2D(pose, desiredCoordinateSystem, LengthUnits.activeFtc());
    }

    public static Pose2D poseToPose2D(Pose pose, CoordinateSystem desiredCoordinateSystem, DistanceUnit unit) {
        Pose converted = pose.getAsCoordinateSystem(desiredCoordinateSystem);
        LengthUnit hardware = LengthUnits.fromFtc(unit);
        double x = LengthUnit.rescale(converted.getX(), LengthUnit.active(), hardware);
        double y = LengthUnit.rescale(converted.getY(), LengthUnit.active(), hardware);
        return new Pose2D(unit, x, y, AngleUnit.RADIANS, converted.getHeading());
    }

    /**
     * Returns a pose from a Pose2D and a coordinate system using the active length unit.
     *
     * @param pose2d the Pose2D object
     * @param coordinateSystem the coordinate system
     * @return a Pose object with x/y in the active follower length unit and heading in radians
     */
    public static Pose pose2DToPose(Pose2D pose2d, CoordinateSystem coordinateSystem) {
        return pose2DToPose(pose2d, coordinateSystem, LengthUnits.activeFtc());
    }

    public static Pose pose2DToPose(Pose2D pose2d, CoordinateSystem coordinateSystem, DistanceUnit unit) {
        LengthUnit hardware = LengthUnits.fromFtc(unit);
        double x = LengthUnit.rescale(pose2d.getX(unit), hardware, LengthUnit.active());
        double y = LengthUnit.rescale(pose2d.getY(unit), hardware, LengthUnit.active());
        return new Pose(x, y, pose2d.getHeading(AngleUnit.RADIANS), coordinateSystem);
    }
}
