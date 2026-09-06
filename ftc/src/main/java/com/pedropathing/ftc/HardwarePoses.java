package com.pedropathing.ftc;

import com.pedropathing.geometry.Pose;
import com.pedropathing.math.LengthUnit;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Pure hardware-boundary pose and velocity conversion used by Pinpoint, OTOS, and tests.
 * Source and destination units are always explicit.
 *
 * @author The Allsparks - 36117
 */
public final class HardwarePoses {
    private HardwarePoses() {}

    public static Pose toFollower(
            double hardwareX,
            double hardwareY,
            double hardwareHeading,
            DistanceUnit hardwareUnit,
            LengthUnit followerUnit,
            AngleUnit hardwareHeadingUnit) {
        return new Pose(
                LengthUnits.toFollower(hardwareX, hardwareUnit, followerUnit),
                LengthUnits.toFollower(hardwareY, hardwareUnit, followerUnit),
                AngleUnit.RADIANS.fromUnit(hardwareHeadingUnit, hardwareHeading));
    }

    public static Pose toFollower(
            double hardwareX,
            double hardwareY,
            double headingRadians,
            DistanceUnit hardwareUnit,
            LengthUnit followerUnit) {
        return toFollower(hardwareX, hardwareY, headingRadians, hardwareUnit, followerUnit, AngleUnit.RADIANS);
    }

    public static double[] toHardware(
            Pose followerPose,
            LengthUnit followerUnit,
            DistanceUnit hardwareUnit,
            AngleUnit hardwareHeadingUnit) {
        return new double[] {
                LengthUnits.toHardware(followerPose.getX(), followerUnit, hardwareUnit),
                LengthUnits.toHardware(followerPose.getY(), followerUnit, hardwareUnit),
                hardwareHeadingUnit.fromUnit(AngleUnit.RADIANS, followerPose.getHeading())
        };
    }
}
