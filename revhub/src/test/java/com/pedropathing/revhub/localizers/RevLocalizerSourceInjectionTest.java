package com.pedropathing.revhub.localizers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.localization.MotionStateSource;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Velocity;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

import org.junit.Test;

public class RevLocalizerSourceInjectionTest {
    private static final double EPS = 1e-6;

    @Test
    public void threeWheelInjectedSourcesAreReadOncePerUpdate() {
        CountingIntSupplier left = new CountingIntSupplier();
        CountingIntSupplier right = new CountingIntSupplier();
        CountingIntSupplier strafe = new CountingIntSupplier();
        ThreeWheelLocalizer localizer = new ThreeWheelLocalizer(threeWheelConfig(), left, right, strafe);
        zero(left, right, strafe);

        left.value = 10;
        right.value = 10;
        localizer.update();
        assertEquals(1, left.calls);
        assertEquals(1, right.calls);
        assertEquals(1, strafe.calls);
        assertEquals(10.0, localizer.pose().x(), EPS);
        assertEquals(0.0, localizer.pose().y(), EPS);
        assertEquals(0.0, localizer.pose().heading(), EPS);

        queryState(localizer);
        assertEquals(1, left.calls);
        assertEquals(1, right.calls);
        assertEquals(1, strafe.calls);

        Pose first = localizer.pose();
        Pose second = localizer.pose();
        assertEquals(first.x(), second.x(), EPS);
        assertEquals(first.y(), second.y(), EPS);
        assertEquals(first.heading(), second.heading(), EPS);
    }

    @Test
    public void twoWheelInjectedSourcesAreReadOncePerUpdate() {
        CountingIntSupplier xPod = new CountingIntSupplier();
        CountingIntSupplier yPod = new CountingIntSupplier();
        CountingDoubleSupplier heading = new CountingDoubleSupplier();
        TwoWheelLocalizer localizer = new TwoWheelLocalizer(twoWheelConfig(), xPod, yPod, heading);
        zero(xPod, yPod);
        heading.calls = 0;

        xPod.value = 10;
        localizer.update();
        assertEquals(1, xPod.calls);
        assertEquals(1, yPod.calls);
        assertEquals(1, heading.calls);
        assertEquals(10.0, localizer.pose().x(), EPS);

        queryState(localizer);
        assertEquals(1, xPod.calls);
        assertEquals(1, yPod.calls);
        assertEquals(1, heading.calls);
    }

    @Test
    public void threeWheelImuInjectedSourcesAreReadOncePerUpdate() {
        boolean previousUseImu = ThreeWheelIMULocalizer.useIMU;
        ThreeWheelIMULocalizer.useIMU = true;
        try {
            CountingIntSupplier left = new CountingIntSupplier();
            CountingIntSupplier right = new CountingIntSupplier();
            CountingIntSupplier strafe = new CountingIntSupplier();
            CountingDoubleSupplier heading = new CountingDoubleSupplier();
            ThreeWheelIMULocalizer localizer = new ThreeWheelIMULocalizer(
                    threeWheelImuConfig(), left, right, strafe, heading);
            zero(left, right, strafe);
            heading.calls = 0;

            heading.value = 0.25;
            localizer.update();
            assertEquals(1, left.calls);
            assertEquals(1, right.calls);
            assertEquals(1, strafe.calls);
            assertEquals(1, heading.calls);
            assertEquals(0.25, localizer.pose().heading(), EPS);

            queryState(localizer);
            assertEquals(1, heading.calls);
        } finally {
            ThreeWheelIMULocalizer.useIMU = previousUseImu;
        }
    }

    @Test
    public void injectedResetRebasesWithoutImplyingAPhysicalEncoderReset() {
        CountingIntSupplier left = new CountingIntSupplier();
        CountingIntSupplier right = new CountingIntSupplier();
        CountingIntSupplier strafe = new CountingIntSupplier();
        ThreeWheelLocalizer localizer = new ThreeWheelLocalizer(threeWheelConfig(), left, right, strafe);

        left.value = 10;
        right.value = 10;
        localizer.update();
        assertEquals(10.0, localizer.pose().x(), EPS);

        zero(left, right, strafe);
        localizer.setPose(new Pose(1, 2, 0.1));
        assertEquals(1, left.calls);
        assertEquals(1, right.calls);
        assertEquals(1, strafe.calls);
        assertEquals(1.0, localizer.pose().x(), EPS);
        assertEquals(2.0, localizer.pose().y(), EPS);

        zero(left, right, strafe);
        localizer.update();
        assertEquals(1, left.calls);
        assertEquals(1.0, localizer.pose().x(), EPS);
    }

    @Test
    public void injectedPedroMultiplierReversesDelta() {
        CountingIntSupplier left = new CountingIntSupplier();
        CountingIntSupplier right = new CountingIntSupplier();
        CountingIntSupplier strafe = new CountingIntSupplier();
        ThreeWheelConfig config = new ThreeWheelConfig(c -> {
            c.leftPodY.set(1.0);
            c.rightPodY.set(-1.0);
            c.strafePodX.set(0.0);
            c.forwardTicksToInches.set(1.0);
            c.strafeTicksToInches.set(1.0);
            c.turnTicksToRadians.set(1.0);
            c.leftEncoderDirection.set(Encoder.REVERSE);
            c.rightEncoderDirection.set(Encoder.REVERSE);
            c.strafeEncoderDirection.set(Encoder.FORWARD);
        });
        ThreeWheelLocalizer localizer = new ThreeWheelLocalizer(config, left, right, strafe);
        left.value = 10;
        right.value = 10;
        localizer.update();
        assertEquals(-10.0, localizer.pose().x(), EPS);
    }

    @Test
    public void legacyHardwareMapConstructorsRemainOnThePublicApi() throws Exception {
        assertNotNull(ThreeWheelLocalizer.class.getConstructor(HardwareMap.class, ThreeWheelConfig.class));
        assertNotNull(TwoWheelLocalizer.class.getConstructor(HardwareMap.class, TwoWheelConfig.class));
        assertNotNull(ThreeWheelIMULocalizer.class.getConstructor(
                HardwareMap.class, ThreeWheelIMUConfig.class));
        assertNotNull(DriveEncoderLocalizer.class.getConstructor(
                HardwareMap.class, DriveEncoderConfig.class));
        assertNotNull(PinpointLocalizer.class.getConstructor(
                HardwareMap.class, PinpointConfig.class));
        assertNotNull(OTOSLocalizer.class.getConstructor(HardwareMap.class, OTOSConfig.class));
        assertNotNull(OctoQuadLocalizer.class.getConstructor(
                HardwareMap.class, OctoQuadConfig.class));
        assertNotNull(Encoder.class.getConstructor(com.qualcomm.robotcore.hardware.DcMotorEx.class));

        assertNotNull(ThreeWheelLocalizer.class.getConstructor(
                ThreeWheelConfig.class, IntSupplier.class, IntSupplier.class, IntSupplier.class));
        assertNotNull(TwoWheelLocalizer.class.getConstructor(
                TwoWheelConfig.class, IntSupplier.class, IntSupplier.class, DoubleSupplier.class));
        assertNotNull(ThreeWheelIMULocalizer.class.getConstructor(
                ThreeWheelIMUConfig.class, IntSupplier.class, IntSupplier.class, IntSupplier.class,
                DoubleSupplier.class));
        assertNotNull(DriveEncoderLocalizer.class.getConstructor(
                DriveEncoderConfig.class, IntSupplier.class, IntSupplier.class, IntSupplier.class,
                IntSupplier.class));
        assertNotNull(PinpointLocalizer.class.getConstructor(MotionStateSource.class));
        assertNotNull(OTOSLocalizer.class.getConstructor(MotionStateSource.class));
        assertNotNull(OctoQuadLocalizer.class.getConstructor(MotionStateSource.class));
    }

    @Test
    public void injectedSourcesConstructWithMethodReferences() {
        class ExternalSnapshot {
            int leftTicks;
            int rightTicks;
            int strafeTicks;

            int leftTicks() {
                return leftTicks;
            }

            int rightTicks() {
                return rightTicks;
            }

            int strafeTicks() {
                return strafeTicks;
            }
        }

        ExternalSnapshot externalSnapshot = new ExternalSnapshot();
        Localizer localizer = new ThreeWheelLocalizer(
                threeWheelConfig(),
                externalSnapshot::leftTicks,
                externalSnapshot::rightTicks,
                externalSnapshot::strafeTicks);
        assertNotNull(localizer.pose());
        assertNotNull(localizer.state());
    }

    @Test
    public void driveEncoderInjectedSourcesAreReadOncePerUpdate() {
        CountingIntSupplier frontLeft = new CountingIntSupplier();
        CountingIntSupplier frontRight = new CountingIntSupplier();
        CountingIntSupplier backLeft = new CountingIntSupplier();
        CountingIntSupplier backRight = new CountingIntSupplier();
        DriveEncoderLocalizer localizer = new DriveEncoderLocalizer(
                driveEncoderConfig(), frontLeft, frontRight, backLeft, backRight);
        zero(frontLeft, frontRight, backLeft, backRight);

        frontLeft.value = 10;
        frontRight.value = 10;
        backLeft.value = 10;
        backRight.value = 10;
        localizer.update();
        assertEquals(1, frontLeft.calls);
        assertEquals(1, frontRight.calls);
        assertEquals(1, backLeft.calls);
        assertEquals(1, backRight.calls);
        assertEquals(40.0, localizer.pose().x(), EPS);
        assertEquals(0.0, localizer.pose().y(), EPS);
        assertEquals(0.0, localizer.pose().heading(), EPS);

        queryState(localizer);
        assertEquals(1, frontLeft.calls);
        assertEquals(1, frontRight.calls);
        assertEquals(1, backLeft.calls);
        assertEquals(1, backRight.calls);
    }

    @Test
    public void driveEncoderInjectedResetIsSoftwareRebase() {
        CountingIntSupplier frontLeft = new CountingIntSupplier();
        CountingIntSupplier frontRight = new CountingIntSupplier();
        CountingIntSupplier backLeft = new CountingIntSupplier();
        CountingIntSupplier backRight = new CountingIntSupplier();
        DriveEncoderLocalizer localizer = new DriveEncoderLocalizer(
                driveEncoderConfig(), frontLeft, frontRight, backLeft, backRight);

        frontLeft.value = 10;
        frontRight.value = 10;
        backLeft.value = 10;
        backRight.value = 10;
        localizer.update();
        assertEquals(40.0, localizer.pose().x(), EPS);

        zero(frontLeft, frontRight, backLeft, backRight);
        localizer.setPose(new Pose(1, 2, 0.1));
        assertEquals(1, frontLeft.calls);
        assertEquals(1.0, localizer.pose().x(), EPS);
        assertEquals(2.0, localizer.pose().y(), EPS);

        zero(frontLeft, frontRight, backLeft, backRight);
        localizer.update();
        assertEquals(1, frontLeft.calls);
        assertEquals(1.0, localizer.pose().x(), EPS);
    }

    @Test
    public void pinpointInjectedSourceIsReadOncePerUpdate() {
        assertInjectedPoseLocalizerReadsOnce(source -> new PinpointLocalizer(source));
    }

    @Test
    public void otosInjectedSourceIsReadOncePerUpdate() {
        assertInjectedPoseLocalizerReadsOnce(source -> new OTOSLocalizer(source));
    }

    @Test
    public void octoQuadInjectedSourceIsReadOncePerUpdate() {
        assertInjectedPoseLocalizerReadsOnce(source -> new OctoQuadLocalizer(source));
    }

    @Test
    public void injectedPoseLocalizerResetAndSetPoseDoNotReadHardware() {
        assertInjectedPoseLocalizerResetAndSetPoseDoNotReadHardware(
                source -> new PinpointLocalizer(source));
        assertInjectedPoseLocalizerResetAndSetPoseDoNotReadHardware(
                source -> new OTOSLocalizer(source));
        assertInjectedPoseLocalizerResetAndSetPoseDoNotReadHardware(
                source -> new OctoQuadLocalizer(source));
    }

    @Test
    public void otosFromSensorAppliesPedroHeadingConvention() {
        MotionState state = OTOSLocalizer.fromSensor(1, 2, Math.PI / 2, 3, 4, 5);
        assertEquals(1.0, state.pose().x(), EPS);
        assertEquals(2.0, state.pose().y(), EPS);
        assertEquals(0.0, state.pose().heading(), EPS);
        assertEquals(3.0, state.velocity().vx, EPS);
        assertEquals(4.0, state.velocity().vy, EPS);
        assertEquals(5.0, state.velocity().omega, EPS);
    }

    private static void assertInjectedPoseLocalizerReadsOnce(
            java.util.function.Function<MotionStateSource, Localizer> factory) {
        CountingMotionStateSource source = new CountingMotionStateSource();
        Localizer localizer = factory.apply(source);
        source.calls = 0;
        source.value = MotionState.ofVelocity(new Pose(1, 2, 0.25), new Velocity(4, 5, 6));

        localizer.update();
        assertEquals(1, source.calls);
        assertEquals(1.0, localizer.pose().x(), EPS);
        assertEquals(2.0, localizer.pose().y(), EPS);
        assertEquals(0.25, localizer.pose().heading(), EPS);
        assertEquals(4.0, localizer.velocity().vx, EPS);
        assertEquals(5.0, localizer.velocity().vy, EPS);
        assertEquals(6.0, localizer.velocity().omega, EPS);

        queryState(localizer);
        assertEquals(1, source.calls);
    }

    private static void assertInjectedPoseLocalizerResetAndSetPoseDoNotReadHardware(
            java.util.function.Function<MotionStateSource, Localizer> factory) {
        CountingMotionStateSource source = new CountingMotionStateSource();
        Localizer localizer = factory.apply(source);
        source.calls = 0;

        localizer.setPose(new Pose(3, 4, 0.2));
        assertEquals(0, source.calls);
        assertEquals(3.0, localizer.pose().x(), EPS);
        assertEquals(4.0, localizer.pose().y(), EPS);

        localizer.reset();
        assertEquals(0, source.calls);
        assertEquals(3.0, localizer.pose().x(), EPS);
    }

    private static void queryState(Localizer localizer) {
        localizer.pose();
        localizer.velocity();
        localizer.twist();
        localizer.state();
        localizer.debug();
    }

    private static void zero(CountingIntSupplier... sources) {
        for (CountingIntSupplier source : sources) {
            source.calls = 0;
        }
    }

    private static ThreeWheelConfig threeWheelConfig() {
        return new ThreeWheelConfig(c -> {
            c.leftPodY.set(1.0);
            c.rightPodY.set(-1.0);
            c.strafePodX.set(0.0);
            c.forwardTicksToInches.set(1.0);
            c.strafeTicksToInches.set(1.0);
            c.turnTicksToRadians.set(1.0);
            c.leftEncoderDirection.set(Encoder.FORWARD);
            c.rightEncoderDirection.set(Encoder.FORWARD);
            c.strafeEncoderDirection.set(Encoder.FORWARD);
        });
    }

    private static TwoWheelConfig twoWheelConfig() {
        return new TwoWheelConfig(c -> {
            c.xPodOffset.set(0.0);
            c.yPodOffset.set(0.0);
            c.forwardTicksToInches.set(1.0);
            c.strafeTicksToInches.set(1.0);
            c.xPodDirection.set(Encoder.FORWARD);
            c.yPodDirection.set(Encoder.FORWARD);
        });
    }

    private static DriveEncoderConfig driveEncoderConfig() {
        return new DriveEncoderConfig(c -> {
            c.robotWidth.set(1.0);
            c.robotLength.set(1.0);
            c.forwardTicksToInches.set(1.0);
            c.strafeTicksToInches.set(1.0);
            c.turnTicksToRadians.set(1.0);
            c.frontLeftDirection.set(Encoder.FORWARD);
            c.frontRightDirection.set(Encoder.FORWARD);
            c.backLeftDirection.set(Encoder.FORWARD);
            c.backRightDirection.set(Encoder.FORWARD);
        });
    }

    private static ThreeWheelIMUConfig threeWheelImuConfig() {
        return new ThreeWheelIMUConfig(c -> {
            c.leftPodY.set(1.0);
            c.rightPodY.set(-1.0);
            c.strafePodX.set(0.0);
            c.forwardTicksToInches.set(1.0);
            c.strafeTicksToInches.set(1.0);
            c.turnTicksToRadians.set(1.0);
            c.leftEncoderDirection.set(Encoder.FORWARD);
            c.rightEncoderDirection.set(Encoder.FORWARD);
            c.strafeEncoderDirection.set(Encoder.FORWARD);
        });
    }
}
