package com.pedropathing.revhub.localizers;

import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.localization.MotionStateSource;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Velocity;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.external.navigation.UnnormalizedAngleUnit;

public class PinpointLocalizer implements Localizer {

    public enum ResetMode {
        RECALIBRATE_IMU,
        RESET_AND_RECALIBRATE_IMU,
        NONE
    }

    private final GoBildaPinpointDriver pinpoint;
    private final DistanceUnit globalDistanceUnit;
    private final ResetMode resetMode;
    private final MotionStateSource source;

    private MotionState motionState;

    public PinpointLocalizer(HardwareMap hardwareMap, PinpointConfig config) {
        this.globalDistanceUnit = config.globalDistanceUnit.get();

        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, config.name.get());

        pinpoint.setOffsets(config.xPodOffset.get(), config.yPodOffset.get(), config.offsetUnits.get());

        if (config.ticksPerUnit.get().isPresent()) {
            pinpoint.setEncoderResolution(config.ticksPerUnit.get().getAsDouble(), config.encoderResolutionUnit.get());
        } else {
            pinpoint.setEncoderResolution(config.podType.get());
        }

        pinpoint.setEncoderDirections(
                config.xPodDirection.get(),
                config.yPodDirection.get()
        );

        resetMode = config.resetMode.get();
        source = this::readDevice;

        reset();
        update();
    }

    /**
     * Creates a localizer from an injected pose/velocity snapshot. Does not read I2C.
     * Reset and setPose are software-only and do not configure or write the Pinpoint.
     *
     * <p>Values must already be in Pedro units (same as the HardwareMap path would report).
     * The callback may come from a cycle-level cache, a simulator, or a replay.
     */
    public PinpointLocalizer(MotionStateSource source) {
        this.pinpoint = null;
        this.globalDistanceUnit = DistanceUnit.INCH;
        this.resetMode = ResetMode.NONE;
        this.source = source;
        this.motionState = MotionState.zero();
        update();
    }

    private MotionState readDevice() {
        pinpoint.update();

        Pose pose = new Pose(
                pinpoint.getPosX(globalDistanceUnit),
                pinpoint.getPosY(globalDistanceUnit),
                pinpoint.getHeading(AngleUnit.RADIANS)
        );

        Velocity velocity = new Velocity(
                pinpoint.getVelX(globalDistanceUnit),
                pinpoint.getVelY(globalDistanceUnit),
                pinpoint.getHeadingVelocity(UnnormalizedAngleUnit.RADIANS)
        );

        return MotionState.ofVelocity(pose, velocity);
    }

    public void setPose(Pose pose) {
        if (pinpoint != null) {
            pinpoint.setPosition(
                    new Pose2D(
                            globalDistanceUnit,
                            pose.x(),
                            pose.y(),
                            AngleUnit.RADIANS,
                            pose.heading()
                    )
            );
        }

        if (motionState != null) {
            motionState = motionState.withPose(pose);
        } else {
            motionState = MotionState.ofVelocity(pose, Velocity.zero());
        }
    }

    @Override
    public void update() {
        motionState = source.state();
    }

    @Override
    public MotionState state() {
        return motionState;
    }

    public void reset() {
        if (pinpoint == null) {
            return;
        }

        if (resetMode == ResetMode.RESET_AND_RECALIBRATE_IMU) {
            pinpoint.resetPosAndIMU();
        } else if (resetMode == ResetMode.RECALIBRATE_IMU) {
            pinpoint.recalibrateIMU();
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
