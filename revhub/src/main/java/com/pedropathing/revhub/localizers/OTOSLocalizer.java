package com.pedropathing.revhub.localizers;

import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.localization.MotionStateSource;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Velocity;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class OTOSLocalizer implements Localizer {
    private final SparkFunOTOS otos;
    private final MotionStateSource source;

    private MotionState motionState;

    public OTOSLocalizer(HardwareMap hardwareMap, OTOSConfig config) {
        otos = hardwareMap.get(SparkFunOTOS.class, config.name.get());

        otos.setLinearUnit(config.linearUnit.get());
        otos.setAngularUnit(AngleUnit.RADIANS);

        SparkFunOTOS.Pose2D offsetPose = new SparkFunOTOS.Pose2D(
                config.offset.get().x(),
                config.offset.get().y(),
                config.offset.get().heading() + Math.PI / 2
        );
        otos.setOffset(offsetPose);
        otos.setLinearScalar(config.linearScalar.get());
        otos.setAngularScalar(config.angularScalar.get());

        otos.calibrateImu();
        otos.resetTracking();

        source = this::readDevice;
        update();
    }

    /**
     * Creates a localizer from an injected pose/velocity snapshot. Does not read I2C.
     * Reset and setPose are software-only and do not configure or write the OTOS.
     *
     * <p>Values must already use Pedro's heading convention. Use {@link #fromSensor} if the
     * snapshot is still in OTOS sensor heading.
     */
    public OTOSLocalizer(MotionStateSource source) {
        this.otos = null;
        this.source = source;
        this.motionState = MotionState.zero();
        update();
    }

    /**
     * Converts OTOS sensor pose/velocity into Pedro's heading convention (subtract π/2).
     */
    public static MotionState fromSensor(double x, double y, double heading,
                                         double vx, double vy, double omega) {
        return MotionState.ofVelocity(
                new Pose(x, y, heading - Math.PI / 2),
                new Velocity(vx, vy, omega));
    }

    private MotionState readDevice() {
        SparkFunOTOS.Pose2D pose2D = otos.getPosition();
        SparkFunOTOS.Pose2D velocity2D = otos.getVelocity();

        return fromSensor(pose2D.x, pose2D.y, pose2D.h, velocity2D.x, velocity2D.y, velocity2D.h);
    }

    public void setPose(Pose pose) {
        if (otos != null) {
            otos.setPosition(
                    new SparkFunOTOS.Pose2D(
                            pose.x(),
                            pose.y(),
                            pose.heading() + Math.PI / 2
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
        if (otos == null) {
            return;
        }
        otos.resetTracking();
    }
}
