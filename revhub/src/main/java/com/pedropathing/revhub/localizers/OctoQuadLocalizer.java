package com.pedropathing.revhub.localizers;

import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.localization.MotionStateSource;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Velocity;
import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import com.qualcomm.hardware.digitalchickenlabs.OctoQuad.*;

public class OctoQuadLocalizer implements Localizer {
    private final LocalizerDataBlock localizer;
    public final OctoQuad octoQuad;
    private final DistanceUnit globalDistanceUnit;
    private final MotionStateSource source;

    private MotionState motionState;

    public OctoQuadLocalizer(HardwareMap hardwareMap, OctoQuadConfig config) {
        octoQuad = hardwareMap.get(OctoQuad.class, config.name.get());
        localizer = new LocalizerDataBlock();

        globalDistanceUnit = config.globalDistanceUnit.get();

        octoQuad.setSingleEncoderDirection(config.xPodPort.get(), config.xPodDirection.get());
        octoQuad.setSingleEncoderDirection(config.yPodPort.get(), config.yPodDirection.get());

        double mmPerUnit = config.encoderResolutionUnit.get().toMm(1.0);
        float ticksPerMM = (float) (config.ticksPerUnit.get() / mmPerUnit);

        octoQuad.setAllLocalizerParameters(
                config.xPodPort.get(),
                config.yPodPort.get(),
                ticksPerMM,
                ticksPerMM,
                (float) -config.offsetUnits.get().toMm(config.xPodOffset.get()),
                (float) -config.offsetUnits.get().toMm(config.yPodOffset.get()),
                config.headingScalar.get().floatValue(),
                config.localizerVelocityIntervalMS.get()
        );
        octoQuad.setI2cRecoveryMode(config.i2cRecoveryMode.get());

        source = this::readDevice;

        reset();

        while (octoQuad.getLocalizerStatus() != LocalizerStatus.RUNNING) {}

        update();
    }

    /**
     * Creates a localizer from an injected pose/velocity snapshot. Does not read I2C.
     * Reset and setPose are software-only and do not configure or write the OctoQuad.
     *
     * <p>Values must already be in Pedro units (same as the HardwareMap path would report).
     */
    public OctoQuadLocalizer(MotionStateSource source) {
        this.octoQuad = null;
        this.localizer = null;
        this.globalDistanceUnit = DistanceUnit.INCH;
        this.source = source;
        this.motionState = MotionState.zero();
        update();
    }

    private MotionState readDevice() {
        octoQuad.readLocalizerData(localizer);

        if (!localizer.isDataValid()) {
            return motionState;
        }

        Pose pose = new Pose(
                globalDistanceUnit.fromMm(localizer.posX_mm),
                globalDistanceUnit.fromMm(localizer.posY_mm),
                localizer.heading_rad
        );

        Velocity velocity = new Velocity(
                globalDistanceUnit.fromMm(localizer.velX_mmS),
                globalDistanceUnit.fromMm(localizer.velY_mmS),
                localizer.velHeading_radS
        );

        return MotionState.ofVelocity(pose, velocity);
    }

    @Override
    public void update() {
        motionState = source.state();
    }

    @Override
    public void setPose(Pose pose) {
        if (octoQuad != null) {
            octoQuad.setLocalizerPose((int) globalDistanceUnit.toMm(pose.x()), (int) globalDistanceUnit.toMm(pose.y()), (float) pose.heading());
        }

        if (motionState != null) {
            motionState = motionState.withPose(pose);
        } else {
            motionState = MotionState.ofVelocity(pose, Velocity.zero());
        }
    }

    @Override
    public MotionState state() {
        return motionState;
    }

    @Override
    public void reset() {
        if (octoQuad == null) {
            return;
        }
        octoQuad.resetLocalizerAndCalibrateIMU();
    }
}
