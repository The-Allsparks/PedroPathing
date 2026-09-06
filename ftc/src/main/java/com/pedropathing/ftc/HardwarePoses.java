package com.pedropathing.ftc;

import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Pure hardware-boundary pose and velocity conversion used by Pinpoint, OTOS, and tests.
 * Pedro poses are always inches and radians. Hardware units are always explicit.
 *
 * @author The Allsparks - 36117
 */
public final class HardwarePoses {
    private HardwarePoses() {}

    public static Pose toInternal(
            double hardwareX,
            double hardwareY,
            double hardwareHeading,
            DistanceUnit hardwareUnit,
            AngleUnit hardwareHeadingUnit) {
        return new Pose(
                HardwareLengths.toInches(hardwareX, hardwareUnit),
                HardwareLengths.toInches(hardwareY, hardwareUnit),
                AngleUnit.RADIANS.fromUnit(hardwareHeadingUnit, hardwareHeading));
    }

    public static Pose toInternal(
            double hardwareX,
            double hardwareY,
            double headingRadians,
            DistanceUnit hardwareUnit) {
        return toInternal(hardwareX, hardwareY, headingRadians, hardwareUnit, AngleUnit.RADIANS);
    }

    public static double[] toHardware(
            Pose internalPose,
            DistanceUnit hardwareUnit,
            AngleUnit hardwareHeadingUnit) {
        return new double[] {
                HardwareLengths.fromInches(internalPose.getX(), hardwareUnit),
                HardwareLengths.fromInches(internalPose.getY(), hardwareUnit),
                hardwareHeadingUnit.fromUnit(AngleUnit.RADIANS, internalPose.getHeading())
        };
    }
}
