package com.pedropathing.ftc.localization.constants;


import com.pedropathing.ftc.localization.Encoder;

/**
 * This is the ThreeWheelConstants class. It holds many constants and parameters for the Three Wheel Localizer.
 * @author Baron Henderson - 20077 The Indubitables
 * @version 1.0, 12/24/2024
 */


public class ThreeWheelConstants {

    /** The number of follower length units per tick of the encoder for forward movement
     * Default Value: .001989436789 */
    public double forwardTicksToDistance = .001989436789;

    /** The number of follower length units per tick of the encoder for lateral movement (strafing)
     * Default Value: .001989436789 */
    public double strafeTicksToDistance = .001989436789;

    /** Encoder ticks to radians for turning.
     * Default Value: .001989436789 */
    public double turnTicksToRadians = .001989436789;

    /** The Y Offset of the Left Encoder (Deadwheel) from the center of the robot
     * Default Value: 1 */
    public double leftPodY = 1;

    /** The Y Offset of the Right Encoder (Deadwheel) from the center of the robot
     * Default Value: -1 */
    public double rightPodY = -1;

    /** The X Offset of the Strafe Encoder (Deadwheel) from the center of the robot
     * Default Value: -2.5 */
    public double strafePodX = -2.5;

    /** The name of the Left Encoder in the hardware map (name of the motor port it is plugged into)
     * Default Value: "leftFront" */
    public String leftEncoder_HardwareMapName = "leftFront";

    /** The name of the Right Encoder in the hardware map (name of the motor port it is plugged into)
     * Default Value: "rightRear" */
    public String rightEncoder_HardwareMapName = "rightRear";

    /** The name of the Strafe Encoder in the hardware map (name of the motor port it is plugged into)
     * Default Value: "rightFront" */
    public String strafeEncoder_HardwareMapName = "rightFront";

    /** The direction of the Left Encoder
     * Default Value: Encoder.REVERSE */
    public double leftEncoderDirection = Encoder.REVERSE;

    /** The direction of the Right Encoder
     * Default Value: Encoder.REVERSE */
    public double rightEncoderDirection = Encoder.REVERSE;

    /** The direction of the Strafe Encoder
     * Default Value: Encoder.FORWARD */
    public double strafeEncoderDirection = Encoder.FORWARD;

    /**
     * This creates a new ThreeWheelConstants with default values.
     */
    public ThreeWheelConstants() {
        defaults();
    }

    public ThreeWheelConstants forwardTicksToDistance(double forwardTicksToDistance) {
        this.forwardTicksToDistance = forwardTicksToDistance;
        return this;
    }

    public ThreeWheelConstants strafeTicksToDistance(double strafeTicksToDistance) {
        this.strafeTicksToDistance = strafeTicksToDistance;
        return this;
    }

    public ThreeWheelConstants turnTicksToRadians(double turnTicksToRadians) {
        this.turnTicksToRadians = turnTicksToRadians;
        return this;
    }

    /** @deprecated use {@link #forwardTicksToDistance(double)} */
    @Deprecated
    public ThreeWheelConstants forwardTicksToInches(double forwardTicksToInches) {
        return forwardTicksToDistance(forwardTicksToInches);
    }

    /** @deprecated use {@link #strafeTicksToDistance(double)} */
    @Deprecated
    public ThreeWheelConstants strafeTicksToInches(double strafeTicksToInches) {
        return strafeTicksToDistance(strafeTicksToInches);
    }

    /** @deprecated use {@link #turnTicksToRadians(double)} */
    @Deprecated
    public ThreeWheelConstants turnTicksToInches(double turnTicksToInches) {
        return turnTicksToRadians(turnTicksToInches);
    }

    /** @deprecated use {@link #turnTicksToRadians(double)} */
    @Deprecated
    public ThreeWheelConstants turnTicksToDistance(double turnTicksToDistance) {
        return turnTicksToRadians(turnTicksToDistance);
    }

    public ThreeWheelConstants leftPodY(double leftPodY) {
        this.leftPodY = leftPodY;
        return this;
    }

    public ThreeWheelConstants rightPodY(double rightPodY) {
        this.rightPodY = rightPodY;
        return this;
    }

    public ThreeWheelConstants strafePodX(double strafePodX) {
        this.strafePodX = strafePodX;
        return this;
    }

    public ThreeWheelConstants leftEncoder_HardwareMapName(String leftEncoder_HardwareMapName) {
        this.leftEncoder_HardwareMapName = leftEncoder_HardwareMapName;
        return this;
    }

    public ThreeWheelConstants rightEncoder_HardwareMapName(String rightEncoder_HardwareMapName) {
        this.rightEncoder_HardwareMapName = rightEncoder_HardwareMapName;
        return this;
    }

    public ThreeWheelConstants strafeEncoder_HardwareMapName(String strafeEncoder_HardwareMapName) {
        this.strafeEncoder_HardwareMapName = strafeEncoder_HardwareMapName;
        return this;
    }

    public ThreeWheelConstants leftEncoderDirection(double leftEncoderDirection) {
        this.leftEncoderDirection = leftEncoderDirection;
        return this;
    }

    public ThreeWheelConstants rightEncoderDirection(double rightEncoderDirection) {
        this.rightEncoderDirection = rightEncoderDirection;
        return this;
    }

    public ThreeWheelConstants strafeEncoderDirection(double strafeEncoderDirection) {
        this.strafeEncoderDirection = strafeEncoderDirection;
        return this;
    }

    public void defaults() {
        forwardTicksToDistance = .001989436789;
        strafeTicksToDistance = .001989436789;
        turnTicksToRadians = .001989436789;
        leftPodY = 1;
        rightPodY = -1;
        strafePodX = -2.5;
        leftEncoder_HardwareMapName = "leftFront";
        rightEncoder_HardwareMapName = "rightRear";
        strafeEncoder_HardwareMapName = "rightFront";
        leftEncoderDirection = Encoder.REVERSE;
        rightEncoderDirection = Encoder.REVERSE;
        strafeEncoderDirection = Encoder.FORWARD;
    }
}
