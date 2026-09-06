package com.pedropathing.ftc.drivetrains;

import com.pedropathing.geometry.Pose;
import com.pedropathing.math.LengthAnchors;
import com.pedropathing.math.LengthUnit;
import com.pedropathing.math.Vector;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

public class MecanumConstants {
    /** The Forward Velocity of the Robot - Different for each robot
     *  Default Value: 81.34056 */
    public  double xVelocity = LengthAnchors.MECANUM_X_VELOCITY;

    /** The Lateral Velocity of the Robot - Different for each robot
     *  Default Value: 65.43028 */
    public  double yVelocity = LengthAnchors.MECANUM_Y_VELOCITY;

    private  double[] convertToPolar = Pose.cartesianToPolar(xVelocity, -yVelocity);

    /** The actual drive vector for the front left wheel, if the robot is facing a heading of 0 radians with the wheel centered at (0,0)
     *  Default Value: new Vector(convertToPolar[0], convertToPolar[1])
     * @implNote This vector should not be changed, but only accessed.
     */
    public  Vector frontLeftVector = new Vector(convertToPolar[0], convertToPolar[1]).normalize();
    public  double maxPower = 1;
    public  String leftFrontMotorName = "leftFront";
    public  String leftRearMotorName = "leftRear";
    public  String rightFrontMotorName = "rightFront";
    public  String rightRearMotorName = "rightRear";
    public  DcMotorSimple.Direction leftFrontMotorDirection = DcMotorSimple.Direction.REVERSE;
    public  DcMotorSimple.Direction leftRearMotorDirection = DcMotorSimple.Direction.REVERSE;
    public  DcMotorSimple.Direction rightFrontMotorDirection = DcMotorSimple.Direction.FORWARD;
    public  DcMotorSimple.Direction rightRearMotorDirection = DcMotorSimple.Direction.FORWARD;
    public  double motorCachingThreshold = 0.01;
    public  boolean useBrakeModeInTeleOp = false;
    public  boolean useVoltageCompensation = false;
    public  double nominalVoltage = 12.0;
    public  double staticFrictionCoefficient = 0.1;
    private LengthUnit appliedLengthUnit = LengthUnit.INCHES;

    public MecanumConstants() {
        defaults();
    }

    public static MecanumConstants defaultsFor(LengthUnit unit) {
        MecanumConstants constants = new MecanumConstants();
        constants.convertTo(LengthUnit.requireNonNull(unit));
        return constants;
    }

    public MecanumConstants xVelocity(double xVelocity) {
        this.xVelocity = xVelocity;
        updateDriveVector();
        return this;
    }

    public MecanumConstants yVelocity(double yVelocity) {
        this.yVelocity = yVelocity;
        updateDriveVector();
        return this;
    }

    /**
     * Convert velocity defaults and previously set tunings from this object's stored unit into
     * {@code unit}. Does not mutate process-wide state.
     */
    public MecanumConstants lengthUnit(LengthUnit unit) {
        return convertTo(unit);
    }

    public MecanumConstants convertTo(LengthUnit unit) {
        LengthUnit.requireNonNull(unit);
        if (unit != appliedLengthUnit) {
            xVelocity = LengthUnit.rescale(xVelocity, appliedLengthUnit, unit);
            yVelocity = LengthUnit.rescale(yVelocity, appliedLengthUnit, unit);
            appliedLengthUnit = unit;
            updateDriveVector();
        }
        return this;
    }

    /**
     * Return a copy whose velocities are expressed in {@code unit}. This object is not mutated.
     */
    public MecanumConstants inUnit(LengthUnit unit) {
        return copy().convertTo(unit);
    }

    public LengthUnit getLengthUnit() {
        return appliedLengthUnit;
    }

    public MecanumConstants copy() {
        MecanumConstants copy = new MecanumConstants();
        copy.xVelocity = xVelocity;
        copy.yVelocity = yVelocity;
        copy.appliedLengthUnit = appliedLengthUnit;
        copy.maxPower = maxPower;
        copy.leftFrontMotorName = leftFrontMotorName;
        copy.leftRearMotorName = leftRearMotorName;
        copy.rightFrontMotorName = rightFrontMotorName;
        copy.rightRearMotorName = rightRearMotorName;
        copy.leftFrontMotorDirection = leftFrontMotorDirection;
        copy.leftRearMotorDirection = leftRearMotorDirection;
        copy.rightFrontMotorDirection = rightFrontMotorDirection;
        copy.rightRearMotorDirection = rightRearMotorDirection;
        copy.motorCachingThreshold = motorCachingThreshold;
        copy.useBrakeModeInTeleOp = useBrakeModeInTeleOp;
        copy.useVoltageCompensation = useVoltageCompensation;
        copy.nominalVoltage = nominalVoltage;
        copy.staticFrictionCoefficient = staticFrictionCoefficient;
        copy.updateDriveVector();
        return copy;
    }

    private void updateDriveVector() {
        convertToPolar = Pose.cartesianToPolar(xVelocity, -yVelocity);
        frontLeftVector = new Vector(convertToPolar[0], convertToPolar[1]).normalize();
    }

    public MecanumConstants maxPower(double maxPower) {
        this.maxPower = maxPower;
        return this;
    }

    public MecanumConstants leftFrontMotorName(String leftFrontMotorName) {
        this.leftFrontMotorName = leftFrontMotorName;
        return this;
    }

    public MecanumConstants leftRearMotorName(String leftRearMotorName) {
        this.leftRearMotorName = leftRearMotorName;
        return this;
    }

    public MecanumConstants rightFrontMotorName(String rightFrontMotorName) {
        this.rightFrontMotorName = rightFrontMotorName;
        return this;
    }

    public MecanumConstants rightRearMotorName(String rightRearMotorName) {
        this.rightRearMotorName = rightRearMotorName;
        return this;
    }

    public MecanumConstants leftFrontMotorDirection(DcMotorSimple.Direction leftFrontMotorDirection) {
        this.leftFrontMotorDirection = leftFrontMotorDirection;
        return this;
    }

    public MecanumConstants leftRearMotorDirection(DcMotorSimple.Direction leftRearMotorDirection) {
        this.leftRearMotorDirection = leftRearMotorDirection;
        return this;
    }

    public MecanumConstants rightFrontMotorDirection(DcMotorSimple.Direction rightFrontMotorDirection) {
        this.rightFrontMotorDirection = rightFrontMotorDirection;
        return this;
    }

    public MecanumConstants rightRearMotorDirection(DcMotorSimple.Direction rightRearMotorDirection) {
        this.rightRearMotorDirection = rightRearMotorDirection;
        return this;
    }

    public MecanumConstants motorCachingThreshold(double motorCachingThreshold) {
        this.motorCachingThreshold = motorCachingThreshold;
        return this;
    }

    public MecanumConstants useBrakeModeInTeleOp(boolean useBrakeModeInTeleOp) {
        this.useBrakeModeInTeleOp = useBrakeModeInTeleOp;
        return this;
    }

    public MecanumConstants useVoltageCompensation(boolean useVoltageCompensation) {
        this.useVoltageCompensation = useVoltageCompensation;
        return this;
    }

    public MecanumConstants nominalVoltage(double nominalVoltage) {
        this.nominalVoltage = nominalVoltage;
        return this;
    }

    public MecanumConstants staticFrictionCoefficient(double staticFrictionCoefficient) {
        this.staticFrictionCoefficient = staticFrictionCoefficient;
        return this;
    }

    public double getXVelocity() {
        return xVelocity;
    }

    public void setXVelocity(double xVelocity) {
        this.xVelocity = xVelocity;
        updateDriveVector();
    }

    public double getYVelocity() {
        return yVelocity;
    }

    public void setYVelocity(double yVelocity) {
        this.yVelocity = yVelocity;
        updateDriveVector();
    }

    public Vector getFrontLeftVector() {
        return frontLeftVector;
    }

    public void setFrontLeftVector(Vector frontLeftVector) {
        this.frontLeftVector = frontLeftVector;
    }

    public double getMaxPower() {
        return maxPower;
    }

    public void setMaxPower(double maxPower) {
        this.maxPower = maxPower;
    }

    public String getLeftFrontMotorName() {
        return leftFrontMotorName;
    }

    public void setLeftFrontMotorName(String leftFrontMotorName) {
        this.leftFrontMotorName = leftFrontMotorName;
    }

    public String getLeftRearMotorName() {
        return leftRearMotorName;
    }

    public void setLeftRearMotorName(String leftRearMotorName) {
        this.leftRearMotorName = leftRearMotorName;
    }

    public String getRightFrontMotorName() {
        return rightFrontMotorName;
    }

    public void setRightFrontMotorName(String rightFrontMotorName) {
        this.rightFrontMotorName = rightFrontMotorName;
    }

    public String getRightRearMotorName() {
        return rightRearMotorName;
    }

    public void setRightRearMotorName(String rightRearMotorName) {
        this.rightRearMotorName = rightRearMotorName;
    }

    public DcMotorSimple.Direction getLeftFrontMotorDirection() {
        return leftFrontMotorDirection;
    }

    public void setLeftFrontMotorDirection(DcMotorSimple.Direction leftFrontMotorDirection) {
        this.leftFrontMotorDirection = leftFrontMotorDirection;
    }

    public DcMotorSimple.Direction getLeftRearMotorDirection() {
        return leftRearMotorDirection;
    }

    public void setLeftRearMotorDirection(DcMotorSimple.Direction leftRearMotorDirection) {
        this.leftRearMotorDirection = leftRearMotorDirection;
    }

    public DcMotorSimple.Direction getRightFrontMotorDirection() {
        return rightFrontMotorDirection;
    }

    public void setRightFrontMotorDirection(DcMotorSimple.Direction rightFrontMotorDirection) {
        this.rightFrontMotorDirection = rightFrontMotorDirection;
    }

    public DcMotorSimple.Direction getRightRearMotorDirection() {
        return rightRearMotorDirection;
    }

    public void setRightRearMotorDirection(DcMotorSimple.Direction rightRearMotorDirection) {
        this.rightRearMotorDirection = rightRearMotorDirection;
    }

    public double getMotorCachingThreshold() {
        return motorCachingThreshold;
    }

    public void setMotorCachingThreshold(double motorCachingThreshold) {
        this.motorCachingThreshold = motorCachingThreshold;
    }

    public boolean isUseBrakeModeInTeleOp() {
        return useBrakeModeInTeleOp;
    }

    public void setUseBrakeModeInTeleOp(boolean useBrakeModeInTeleOp) {
        this.useBrakeModeInTeleOp = useBrakeModeInTeleOp;
    }

    /**
     * This method sets the default values for the MecanumConstants class.
     * It is called in the constructor of the MecanumConstants class.
     */
    public void defaults() {
        xVelocity = LengthAnchors.MECANUM_X_VELOCITY;
        yVelocity = LengthAnchors.MECANUM_Y_VELOCITY;
        convertToPolar = Pose.cartesianToPolar(xVelocity, -yVelocity);
        frontLeftVector = new Vector(convertToPolar[0], convertToPolar[1]).normalize();
        maxPower = 1;
        leftFrontMotorName = "leftFront";
        leftRearMotorName = "leftRear";
        rightFrontMotorName = "rightFront";
        rightRearMotorName = "rightRear";
        leftFrontMotorDirection = DcMotorSimple.Direction.REVERSE;
        leftRearMotorDirection = DcMotorSimple.Direction.REVERSE;
        rightFrontMotorDirection = DcMotorSimple.Direction.FORWARD;
        rightRearMotorDirection = DcMotorSimple.Direction.FORWARD;
        motorCachingThreshold = 0.01;
        useBrakeModeInTeleOp = false;
        useVoltageCompensation = false;
        nominalVoltage = 12.0;
        staticFrictionCoefficient = 0.1;
        appliedLengthUnit = LengthUnit.INCHES;
    }
}
