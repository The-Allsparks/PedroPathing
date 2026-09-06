package com.pedropathing.ftc;

import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.drivetrains.SwerveConstants;
import com.pedropathing.ftc.localization.constants.DriveEncoderConstants;
import com.pedropathing.ftc.localization.constants.ThreeWheelConstants;
import com.pedropathing.ftc.localization.constants.ThreeWheelIMUConstants;
import com.pedropathing.ftc.localization.constants.TwoWheelConstants;
import com.pedropathing.math.PedroUnits;

/**
 * Converts TeamCode-facing hardware and follower constants into Pedro's internal inches,
 * kilograms, and radians. Unchanged Pedro library defaults are left canonical so
 * {@code setUnits} does not reinterpret Strafer inch defaults as centimeters.
 */
final class ConfiguredHardwareUnits {
    private ConfiguredHardwareUnits() {}

    static FollowerConstants toInternal(FollowerConstants source, PedroUnits units) {
        if (source == null || PedroUnits.DEFAULT.equals(units)) {
            return source;
        }
        FollowerConstants defaults = new FollowerConstants();
        FollowerConstants copy = source.copy();
        boolean changed = false;
        if (PedroUnits.differs(source.mass, defaults.mass)) {
            copy.mass(units.massToInternal(source.mass));
            changed = true;
        }
        if (PedroUnits.differs(source.forwardZeroPowerAcceleration, defaults.forwardZeroPowerAcceleration)) {
            copy.forwardZeroPowerAcceleration(units.accelerationToInternal(source.forwardZeroPowerAcceleration));
            changed = true;
        }
        if (PedroUnits.differs(source.lateralZeroPowerAcceleration, defaults.lateralZeroPowerAcceleration)) {
            copy.lateralZeroPowerAcceleration(units.accelerationToInternal(source.lateralZeroPowerAcceleration));
            changed = true;
        }
        if (PedroUnits.differs(source.stuckVelocity, defaults.stuckVelocity)) {
            copy.stuckVelocity = units.velocityToInternal(source.stuckVelocity);
            changed = true;
        }
        if (PedroUnits.differs(source.translationalPIDFSwitch, defaults.translationalPIDFSwitch)) {
            copy.translationalPIDFSwitch = units.lengthToInternal(source.translationalPIDFSwitch);
            changed = true;
        }
        if (PedroUnits.differs(source.drivePIDFSwitch, defaults.drivePIDFSwitch)) {
            copy.drivePIDFSwitch = units.velocityToInternal(source.drivePIDFSwitch);
            changed = true;
        }
        if (PedroUnits.differs(source.headingPIDFSwitch, defaults.headingPIDFSwitch)) {
            copy.headingPIDFSwitch = units.angleToInternal(source.headingPIDFSwitch);
            changed = true;
        }
        if (PedroUnits.differs(source.turnHeadingErrorThreshold, defaults.turnHeadingErrorThreshold)) {
            copy.turnHeadingErrorThreshold = units.angleToInternal(source.turnHeadingErrorThreshold);
            changed = true;
        }
        return changed ? copy : source;
    }

    static MecanumConstants toInternal(MecanumConstants source, PedroUnits units) {
        if (source == null || PedroUnits.DEFAULT.equals(units)) {
            return source;
        }
        MecanumConstants defaults = new MecanumConstants();
        MecanumConstants copy = source.copy();
        boolean changed = false;
        if (PedroUnits.differs(source.xVelocity, defaults.xVelocity)) {
            copy.xVelocity(units.velocityToInternal(source.xVelocity));
            changed = true;
        }
        if (PedroUnits.differs(source.yVelocity, defaults.yVelocity)) {
            copy.yVelocity(units.velocityToInternal(source.yVelocity));
            changed = true;
        }
        return changed ? copy : source;
    }

    static SwerveConstants toInternal(SwerveConstants source, PedroUnits units) {
        if (source == null || PedroUnits.DEFAULT.equals(units)) {
            return source;
        }
        SwerveConstants defaults = new SwerveConstants();
        SwerveConstants copy = source.copy();
        boolean changed = false;
        if (PedroUnits.differs(source.xVelocity, defaults.xVelocity)) {
            copy.xVelocity(units.velocityToInternal(source.xVelocity));
            changed = true;
        }
        if (PedroUnits.differs(source.yVelocity, defaults.yVelocity)) {
            copy.yVelocity(units.velocityToInternal(source.yVelocity));
            changed = true;
        }
        return changed ? copy : source;
    }

    static DriveEncoderConstants toInternal(DriveEncoderConstants source, PedroUnits units) {
        if (source == null || PedroUnits.DEFAULT.equals(units)) {
            return source;
        }
        DriveEncoderConstants defaults = new DriveEncoderConstants();
        DriveEncoderConstants copy = copyDriveEncoder(source);
        boolean changed = false;
        if (PedroUnits.differs(source.forwardTicksToInches, defaults.forwardTicksToInches)) {
            copy.forwardTicksToInches = units.lengthToInternal(source.forwardTicksToInches);
            changed = true;
        }
        if (PedroUnits.differs(source.strafeTicksToInches, defaults.strafeTicksToInches)) {
            copy.strafeTicksToInches = units.lengthToInternal(source.strafeTicksToInches);
            changed = true;
        }
        if (PedroUnits.differs(source.turnTicksToInches, defaults.turnTicksToInches)) {
            copy.turnTicksToInches = units.angleToInternal(source.turnTicksToInches);
            changed = true;
        }
        if (PedroUnits.differs(source.robot_Width, defaults.robot_Width)) {
            copy.robot_Width = units.lengthToInternal(source.robot_Width);
            changed = true;
        }
        if (PedroUnits.differs(source.robot_Length, defaults.robot_Length)) {
            copy.robot_Length = units.lengthToInternal(source.robot_Length);
            changed = true;
        }
        return changed ? copy : source;
    }

    static TwoWheelConstants toInternal(TwoWheelConstants source, PedroUnits units) {
        if (source == null || PedroUnits.DEFAULT.equals(units)) {
            return source;
        }
        TwoWheelConstants defaults = new TwoWheelConstants();
        TwoWheelConstants copy = copyTwoWheel(source);
        boolean changed = false;
        if (PedroUnits.differs(source.forwardTicksToInches, defaults.forwardTicksToInches)) {
            copy.forwardTicksToInches = units.lengthToInternal(source.forwardTicksToInches);
            changed = true;
        }
        if (PedroUnits.differs(source.strafeTicksToInches, defaults.strafeTicksToInches)) {
            copy.strafeTicksToInches = units.lengthToInternal(source.strafeTicksToInches);
            changed = true;
        }
        if (PedroUnits.differs(source.forwardPodY, defaults.forwardPodY)) {
            copy.forwardPodY = units.lengthToInternal(source.forwardPodY);
            changed = true;
        }
        if (PedroUnits.differs(source.strafePodX, defaults.strafePodX)) {
            copy.strafePodX = units.lengthToInternal(source.strafePodX);
            changed = true;
        }
        return changed ? copy : source;
    }

    static ThreeWheelConstants toInternal(ThreeWheelConstants source, PedroUnits units) {
        if (source == null || PedroUnits.DEFAULT.equals(units)) {
            return source;
        }
        ThreeWheelConstants defaults = new ThreeWheelConstants();
        ThreeWheelConstants copy = copyThreeWheel(source);
        boolean changed = false;
        if (PedroUnits.differs(source.forwardTicksToInches, defaults.forwardTicksToInches)) {
            copy.forwardTicksToInches = units.lengthToInternal(source.forwardTicksToInches);
            changed = true;
        }
        if (PedroUnits.differs(source.strafeTicksToInches, defaults.strafeTicksToInches)) {
            copy.strafeTicksToInches = units.lengthToInternal(source.strafeTicksToInches);
            changed = true;
        }
        if (PedroUnits.differs(source.turnTicksToInches, defaults.turnTicksToInches)) {
            copy.turnTicksToInches = units.angleToInternal(source.turnTicksToInches);
            changed = true;
        }
        if (PedroUnits.differs(source.leftPodY, defaults.leftPodY)) {
            copy.leftPodY = units.lengthToInternal(source.leftPodY);
            changed = true;
        }
        if (PedroUnits.differs(source.rightPodY, defaults.rightPodY)) {
            copy.rightPodY = units.lengthToInternal(source.rightPodY);
            changed = true;
        }
        if (PedroUnits.differs(source.strafePodX, defaults.strafePodX)) {
            copy.strafePodX = units.lengthToInternal(source.strafePodX);
            changed = true;
        }
        return changed ? copy : source;
    }

    static ThreeWheelIMUConstants toInternal(ThreeWheelIMUConstants source, PedroUnits units) {
        if (source == null || PedroUnits.DEFAULT.equals(units)) {
            return source;
        }
        ThreeWheelIMUConstants defaults = new ThreeWheelIMUConstants();
        ThreeWheelIMUConstants copy = copyThreeWheelImu(source);
        boolean changed = false;
        if (PedroUnits.differs(source.forwardTicksToInches, defaults.forwardTicksToInches)) {
            copy.forwardTicksToInches = units.lengthToInternal(source.forwardTicksToInches);
            changed = true;
        }
        if (PedroUnits.differs(source.strafeTicksToInches, defaults.strafeTicksToInches)) {
            copy.strafeTicksToInches = units.lengthToInternal(source.strafeTicksToInches);
            changed = true;
        }
        if (PedroUnits.differs(source.turnTicksToInches, defaults.turnTicksToInches)) {
            copy.turnTicksToInches = units.angleToInternal(source.turnTicksToInches);
            changed = true;
        }
        if (PedroUnits.differs(source.leftPodY, defaults.leftPodY)) {
            copy.leftPodY = units.lengthToInternal(source.leftPodY);
            changed = true;
        }
        if (PedroUnits.differs(source.rightPodY, defaults.rightPodY)) {
            copy.rightPodY = units.lengthToInternal(source.rightPodY);
            changed = true;
        }
        if (PedroUnits.differs(source.strafePodX, defaults.strafePodX)) {
            copy.strafePodX = units.lengthToInternal(source.strafePodX);
            changed = true;
        }
        return changed ? copy : source;
    }

    private static DriveEncoderConstants copyDriveEncoder(DriveEncoderConstants source) {
        DriveEncoderConstants copy = new DriveEncoderConstants();
        copy.forwardTicksToInches = source.forwardTicksToInches;
        copy.strafeTicksToInches = source.strafeTicksToInches;
        copy.turnTicksToInches = source.turnTicksToInches;
        copy.robot_Width = source.robot_Width;
        copy.robot_Length = source.robot_Length;
        copy.leftFrontEncoderDirection = source.leftFrontEncoderDirection;
        copy.rightFrontEncoderDirection = source.rightFrontEncoderDirection;
        copy.leftRearEncoderDirection = source.leftRearEncoderDirection;
        copy.rightRearEncoderDirection = source.rightRearEncoderDirection;
        copy.leftFrontMotorName = source.leftFrontMotorName;
        copy.leftRearMotorName = source.leftRearMotorName;
        copy.rightFrontMotorName = source.rightFrontMotorName;
        copy.rightRearMotorName = source.rightRearMotorName;
        return copy;
    }

    private static TwoWheelConstants copyTwoWheel(TwoWheelConstants source) {
        TwoWheelConstants copy = new TwoWheelConstants();
        copy.forwardTicksToInches = source.forwardTicksToInches;
        copy.strafeTicksToInches = source.strafeTicksToInches;
        copy.forwardPodY = source.forwardPodY;
        copy.strafePodX = source.strafePodX;
        copy.IMU_HardwareMapName = source.IMU_HardwareMapName;
        copy.forwardEncoder_HardwareMapName = source.forwardEncoder_HardwareMapName;
        copy.strafeEncoder_HardwareMapName = source.strafeEncoder_HardwareMapName;
        copy.IMU_Orientation = source.IMU_Orientation;
        copy.forwardEncoderDirection = source.forwardEncoderDirection;
        copy.strafeEncoderDirection = source.strafeEncoderDirection;
        copy.imu = source.imu;
        return copy;
    }

    private static ThreeWheelConstants copyThreeWheel(ThreeWheelConstants source) {
        ThreeWheelConstants copy = new ThreeWheelConstants();
        copy.forwardTicksToInches = source.forwardTicksToInches;
        copy.strafeTicksToInches = source.strafeTicksToInches;
        copy.turnTicksToInches = source.turnTicksToInches;
        copy.leftPodY = source.leftPodY;
        copy.rightPodY = source.rightPodY;
        copy.strafePodX = source.strafePodX;
        copy.leftEncoder_HardwareMapName = source.leftEncoder_HardwareMapName;
        copy.rightEncoder_HardwareMapName = source.rightEncoder_HardwareMapName;
        copy.strafeEncoder_HardwareMapName = source.strafeEncoder_HardwareMapName;
        copy.leftEncoderDirection = source.leftEncoderDirection;
        copy.rightEncoderDirection = source.rightEncoderDirection;
        copy.strafeEncoderDirection = source.strafeEncoderDirection;
        return copy;
    }

    private static ThreeWheelIMUConstants copyThreeWheelImu(ThreeWheelIMUConstants source) {
        ThreeWheelIMUConstants copy = new ThreeWheelIMUConstants();
        copy.forwardTicksToInches = source.forwardTicksToInches;
        copy.strafeTicksToInches = source.strafeTicksToInches;
        copy.turnTicksToInches = source.turnTicksToInches;
        copy.leftPodY = source.leftPodY;
        copy.rightPodY = source.rightPodY;
        copy.strafePodX = source.strafePodX;
        copy.IMU_HardwareMapName = source.IMU_HardwareMapName;
        copy.leftEncoder_HardwareMapName = source.leftEncoder_HardwareMapName;
        copy.rightEncoder_HardwareMapName = source.rightEncoder_HardwareMapName;
        copy.strafeEncoder_HardwareMapName = source.strafeEncoder_HardwareMapName;
        copy.IMU_Orientation = source.IMU_Orientation;
        copy.leftEncoderDirection = source.leftEncoderDirection;
        copy.rightEncoderDirection = source.rightEncoderDirection;
        copy.strafeEncoderDirection = source.strafeEncoderDirection;
        copy.imu = source.imu;
        return copy;
    }
}
