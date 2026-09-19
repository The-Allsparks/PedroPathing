package com.pedropathing.revhub.localizers;

import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Velocity;
import com.pedropathing.utils.Timer;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.function.IntSupplier;

/**
 * Localizer that uses the four drive-motor encoders. Pose exponential math matches the
 * Pedro 2 drive-encoder localizer.
 */
public class DriveEncoderLocalizer implements Localizer {
    private final Encoder frontLeft;
    private final Encoder frontRight;
    private final Encoder backLeft;
    private final Encoder backRight;

    private final double robotWidth;
    private final double robotLength;
    private final double forwardTicksToInches;
    private final double strafeTicksToInches;
    private final double turnTicksToRadians;

    private MotionState motionState;
    private Pose pose = Pose.zero();
    private double totalHeading = 0;

    private final Timer timer;

    /**
     * Creates a motor-backed localizer from a HardwareMap. Encoders are constructed from the
     * configured motor names and then delegated to the source-injection path.
     */
    public DriveEncoderLocalizer(HardwareMap map, DriveEncoderConfig config) {
        this(config,
                new Encoder(map.get(DcMotorEx.class, config.frontLeftName.get())),
                new Encoder(map.get(DcMotorEx.class, config.frontRightName.get())),
                new Encoder(map.get(DcMotorEx.class, config.backLeftName.get())),
                new Encoder(map.get(DcMotorEx.class, config.backRightName.get())));
    }

    /**
     * Creates a localizer from injected encoder position sources. Each source is raw
     * hardware-sign ticks; Pedro applies the directions in {@code config}. Reset rebases a
     * software zero and does not physically reset encoders.
     */
    public DriveEncoderLocalizer(DriveEncoderConfig config, IntSupplier frontLeftPosition,
                                 IntSupplier frontRightPosition, IntSupplier backLeftPosition,
                                 IntSupplier backRightPosition) {
        this(config,
                Encoder.from(frontLeftPosition),
                Encoder.from(frontRightPosition),
                Encoder.from(backLeftPosition),
                Encoder.from(backRightPosition));
    }

    private DriveEncoderLocalizer(DriveEncoderConfig config, Encoder frontLeft, Encoder frontRight,
                                  Encoder backLeft, Encoder backRight) {
        this.forwardTicksToInches = config.forwardTicksToInches.get();
        this.strafeTicksToInches = config.strafeTicksToInches.get();
        this.turnTicksToRadians = config.turnTicksToRadians.get();
        this.robotWidth = config.robotWidth.get();
        this.robotLength = config.robotLength.get();

        this.frontLeft = frontLeft;
        this.frontRight = frontRight;
        this.backLeft = backLeft;
        this.backRight = backRight;

        this.frontLeft.setDirection(config.frontLeftDirection.get());
        this.frontRight.setDirection(config.frontRightDirection.get());
        this.backLeft.setDirection(config.backLeftDirection.get());
        this.backRight.setDirection(config.backRightDirection.get());

        this.timer = new Timer();
        this.motionState = MotionState.ofVelocity(pose, Velocity.zero());
        update();
    }

    @Override
    public void setPose(Pose setPose) {
        this.pose = setPose;
        frontLeft.reset();
        frontRight.reset();
        backLeft.reset();
        backRight.reset();

        if (motionState != null) {
            motionState = motionState.withPose(setPose);
        } else {
            motionState = MotionState.ofVelocity(setPose, Velocity.zero());
        }
    }

    @Override
    public void update() {
        long deltaTimeNano = timer.nanoseconds();
        timer.reset();

        frontLeft.update();
        frontRight.update();
        backLeft.update();
        backRight.update();

        double deltaFrontLeft = frontLeft.getDeltaPosition();
        double deltaFrontRight = frontRight.getDeltaPosition();
        double deltaBackLeft = backLeft.getDeltaPosition();
        double deltaBackRight = backRight.getDeltaPosition();

        double deltaX = forwardTicksToInches
                * (deltaFrontLeft + deltaFrontRight + deltaBackLeft + deltaBackRight);
        double deltaY = strafeTicksToInches
                * (-deltaFrontLeft + deltaFrontRight + deltaBackLeft - deltaBackRight);
        double deltaRadians = turnTicksToRadians
                * ((-deltaFrontLeft + deltaFrontRight - deltaBackLeft + deltaBackRight)
                / (robotWidth + robotLength));

        Matrix prevRotationMatrix = Matrix.rotationTransform(pose.heading());

        Matrix transformation;
        if (Math.abs(deltaRadians) < 0.001) {
            double term = 1.0 - (Math.pow(deltaRadians, 2) / 6.0);
            double halfDelta = deltaRadians / 2.0;
            transformation = new Matrix(new double[][]{
                    {term, -halfDelta, 0.0},
                    {halfDelta, term, 0.0},
                    {0.0, 0.0, 1.0}
            });
        } else {
            double sinD = Math.sin(deltaRadians);
            double cosD = Math.cos(deltaRadians);
            transformation = new Matrix(new double[][]{
                    {sinD / deltaRadians, (cosD - 1.0) / deltaRadians, 0.0},
                    {(1.0 - cosD) / deltaRadians, sinD / deltaRadians, 0.0},
                    {0.0, 0.0, 1.0}
            });
        }

        Matrix robotDeltas = new Matrix(new double[][]{
                {deltaX},
                {deltaY},
                {deltaRadians}
        });

        Matrix globalDeltas = prevRotationMatrix.times(transformation).times(robotDeltas);

        pose = pose.plus(new Pose(globalDeltas.get(0, 0), globalDeltas.get(1, 0), globalDeltas.get(2, 0)));
        totalHeading += globalDeltas.get(2, 0);

        double dtSeconds = deltaTimeNano / 1e9;
        Velocity currentVelocity = new Velocity(
                globalDeltas.get(0, 0) / dtSeconds,
                globalDeltas.get(1, 0) / dtSeconds,
                globalDeltas.get(2, 0) / dtSeconds
        );

        motionState = MotionState.ofVelocity(pose, currentVelocity);
    }

    @Override
    public MotionState state() {
        return motionState;
    }

    public double getTotalHeading() {
        return totalHeading;
    }

    @Override
    public void reset() {
        frontLeft.reset();
        frontRight.reset();
        backLeft.reset();
        backRight.reset();
        pose = Pose.zero();
        totalHeading = 0;
    }
}
