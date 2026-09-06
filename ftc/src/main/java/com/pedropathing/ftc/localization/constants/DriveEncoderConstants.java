package com.pedropathing.ftc.localization.constants;

import com.pedropathing.ftc.localization.Encoder;

public class DriveEncoderConstants {
    /**
     * Encoder ticks to follower {@link com.pedropathing.math.LengthUnit} for forward travel.
     */
    public double forwardTicksToDistance = 1;
    /**
     * Encoder ticks to follower length unit for strafe travel.
     */
    public double strafeTicksToDistance = 1;
    /**
     * Encoder ticks to radians for turning. The upstream name said inches; this
     * value was always a heading scale.
     */
    public double turnTicksToRadians = 1;

    public double robot_Width = 1;
    public double robot_Length = 1;

    public double leftFrontEncoderDirection = Encoder.REVERSE;
    public double rightFrontEncoderDirection = Encoder.FORWARD;
    public double leftRearEncoderDirection = Encoder.REVERSE;
    public double rightRearEncoderDirection = Encoder.FORWARD;

    public String leftFrontMotorName = "leftFront";
    public String leftRearMotorName = "leftRear";
    public String rightFrontMotorName = "rightFront";
    public String rightRearMotorName = "rightRear";

    public DriveEncoderConstants forwardTicksToDistance(double forwardTicksToDistance) {
        this.forwardTicksToDistance = forwardTicksToDistance;
        return this;
    }

    public DriveEncoderConstants strafeTicksToDistance(double strafeTicksToDistance) {
        this.strafeTicksToDistance = strafeTicksToDistance;
        return this;
    }

    public DriveEncoderConstants turnTicksToRadians(double turnTicksToRadians) {
        this.turnTicksToRadians = turnTicksToRadians;
        return this;
    }

    /** @deprecated use {@link #forwardTicksToDistance(double)} */
    @Deprecated
    public DriveEncoderConstants forwardTicksToInches(double forwardTicksToInches) {
        return forwardTicksToDistance(forwardTicksToInches);
    }

    /** @deprecated use {@link #strafeTicksToDistance(double)} */
    @Deprecated
    public DriveEncoderConstants strafeTicksToInches(double strafeTicksToInches) {
        return strafeTicksToDistance(strafeTicksToInches);
    }

    /** @deprecated use {@link #turnTicksToRadians(double)} */
    @Deprecated
    public DriveEncoderConstants turnTicksToInches(double turnTicksToInches) {
        return turnTicksToRadians(turnTicksToInches);
    }

    /** @deprecated use {@link #turnTicksToRadians(double)} */
    @Deprecated
    public DriveEncoderConstants turnTicksToDistance(double turnTicksToDistance) {
        return turnTicksToRadians(turnTicksToDistance);
    }

    public DriveEncoderConstants robotWidth(double robot_Width) {
        this.robot_Width = robot_Width;
        return this;
    }

    public DriveEncoderConstants robotLength(double robot_Length) {
        this.robot_Length = robot_Length;
        return this;
    }

    public DriveEncoderConstants leftFrontEncoderDirection(double leftFrontEncoderDirection) {
        this.leftFrontEncoderDirection = leftFrontEncoderDirection;
        return this;
    }

    public DriveEncoderConstants rightFrontEncoderDirection(double rightFrontEncoderDirection) {
        this.rightFrontEncoderDirection = rightFrontEncoderDirection;
        return this;
    }

    public DriveEncoderConstants leftRearEncoderDirection(double leftRearEncoderDirection) {
        this.leftRearEncoderDirection = leftRearEncoderDirection;
        return this;
    }

    public DriveEncoderConstants rightRearEncoderDirection(double rightRearEncoderDirection) {
        this.rightRearEncoderDirection = rightRearEncoderDirection;
        return this;
    }

    public DriveEncoderConstants leftFrontMotorName(String leftFrontMotorName) {
        this.leftFrontMotorName = leftFrontMotorName;
        return this;
    }

    public DriveEncoderConstants leftRearMotorName(String leftRearMotorName) {
        this.leftRearMotorName = leftRearMotorName;
        return this;
    }

    public DriveEncoderConstants rightFrontMotorName(String rightFrontMotorName) {
        this.rightFrontMotorName = rightFrontMotorName;
        return this;
    }

    public DriveEncoderConstants rightRearMotorName(String rightRearMotorName) {
        this.rightRearMotorName = rightRearMotorName;
        return this;
    }

    public void defaults() {
        forwardTicksToDistance = 1;
        strafeTicksToDistance = 1;
        turnTicksToRadians = 1;

        robot_Width = 1;
        robot_Length = 1;

        leftFrontEncoderDirection = Encoder.REVERSE;
        rightFrontEncoderDirection = Encoder.FORWARD;
        leftRearEncoderDirection = Encoder.REVERSE;
        rightRearEncoderDirection = Encoder.FORWARD;

        leftFrontMotorName = "leftFront";
        leftRearMotorName = "leftRear";
        rightFrontMotorName = "rightFront";
        rightRearMotorName = "rightRear";
    }
}