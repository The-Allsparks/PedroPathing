package com.pedropathing.ftc;

import com.pedropathing.geometry.CoordinateSystem;
import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

/**
 * Converts between canonical Pedro {@link Pose} values (inches and radians) and FTC {@link Pose2D}
 * values in an explicit hardware {@link DistanceUnit}.
 *
 * <p>Feet and other user-interface units are converted to inches before a pose reaches this class.
 */
public class PoseConverter {

    /**
     * Converts a Pose to a Pose2D in the desired coordinate system.
     *
     * @param pose the Pose object, with x/y in inches
     * @param desiredCoordinateSystem the desired coordinate system
     * @return a Pose2D object with the x and y coordinates in inches and the heading in radians
     */
    public static Pose2D poseToPose2D(Pose pose, CoordinateSystem desiredCoordinateSystem) {
        return poseToPose2D(pose, desiredCoordinateSystem, DistanceUnit.INCH);
    }

    /**
     * Converts a canonical Pedro {@link Pose} (inches, radians) to an FTC {@link Pose2D} in
     * {@code hardwareDistanceUnit}.
     */
    public static Pose2D poseToPose2D(
            Pose pose,
            CoordinateSystem desiredCoordinateSystem,
            DistanceUnit hardwareDistanceUnit) {
        Pose converted = pose.getAsCoordinateSystem(desiredCoordinateSystem);
        double x = HardwareLengths.fromInches(converted.getX(), hardwareDistanceUnit);
        double y = HardwareLengths.fromInches(converted.getY(), hardwareDistanceUnit);
        return new Pose2D(hardwareDistanceUnit, x, y, AngleUnit.RADIANS, converted.getHeading());
    }

    /**
     * Returns a pose from a Pose2D and a coordinate system.
     *
     * @param pose2d the Pose2D object
     * @param coordinateSystem the coordinate system
     * @return a Pose object with the x and y coordinates in inches and the heading in radians
     */
    public static Pose pose2DToPose(Pose2D pose2d, CoordinateSystem coordinateSystem) {
        return pose2DToPose(pose2d, coordinateSystem, DistanceUnit.INCH);
    }

    /**
     * Converts an FTC {@link Pose2D} in {@code hardwareDistanceUnit} to a canonical Pedro
     * {@link Pose} (inches, radians).
     */
    public static Pose pose2DToPose(
            Pose2D pose2d,
            CoordinateSystem coordinateSystem,
            DistanceUnit hardwareDistanceUnit) {
        double x = HardwareLengths.toInches(pose2d.getX(hardwareDistanceUnit), hardwareDistanceUnit);
        double y = HardwareLengths.toInches(pose2d.getY(hardwareDistanceUnit), hardwareDistanceUnit);
        return new Pose(x, y, pose2d.getHeading(AngleUnit.RADIANS), coordinateSystem);
    }
}
