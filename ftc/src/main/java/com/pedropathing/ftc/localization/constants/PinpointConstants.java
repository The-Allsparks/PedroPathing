package com.pedropathing.ftc.localization.constants;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;

import com.pedropathing.ftc.HardwareLengths;
import com.pedropathing.math.LengthUnit;
import com.pedropathing.math.PedroUnits;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.OptionalDouble;

/**
 * This is the PinpointConstants class. It holds many constants and parameters for the Pinpoint Localizer.
 * @author Baron Henderson - 20077 The Indubitables
 * @version 1.0, 12/24/2024
 */

@TargetApi(Build.VERSION_CODES.N)
public class PinpointConstants {

    static final double DEFAULT_FORWARD_POD_Y = 1.0;
    static final double DEFAULT_STRAFE_POD_X = -2.5;
    static final double MAX_POD_OFFSET_INCHES = 24.0;

    /** The Y Offset of the Forward Encoder (Deadwheel) from the center of the robot in DistanceUnit
     * @see #distanceUnit
     * Default Value: 1 */
    public  double forwardPodY = DEFAULT_FORWARD_POD_Y;

    /** The X Offset of the Strafe Encoder (Deadwheel) from the center of the robot in DistanceUnit
     * @see #distanceUnit
     * Default Value: -2.5 */
    public  double strafePodX = DEFAULT_STRAFE_POD_X;

    /** The Unit of Distance that the Pinpoint uses to measure distance
     * Default Value: DistanceUnit.INCH */
    public  DistanceUnit distanceUnit = DistanceUnit.INCH;

    /** The name of the Pinpoint in the hardware map (name of the I2C port it is plugged into)
     * Default Value: "pinpoint" */
    public  String hardwareMapName = "pinpoint";

    /** Custom Yaw Scalar for the Pinpoint (overrides the calibration of the Pinpoint) */
    @SuppressLint("NewApi")
    public OptionalDouble yawScalar = OptionalDouble.empty();

    /** The Encoder Resolution for the Pinpoint. Used by default, but can be changed to a custom resolution.
     * Default Value: GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD */
    public  GoBildaPinpointDriver.GoBildaOdometryPods encoderResolution = GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD;

    /** The Encoder Resolution for the Pinpoint. Unused by default, but can be used if you want to use a custom encoder resolution. */
    @SuppressLint("NewApi")
    public OptionalDouble customEncoderResolution = OptionalDouble.empty();

    /** The Encoder Direction for the Forward Encoder (Deadwheel)
     * Default Value: GoBildaPinpointDriver.EncoderDirection.REVERSED */
    public  GoBildaPinpointDriver.EncoderDirection forwardEncoderDirection = GoBildaPinpointDriver.EncoderDirection.REVERSED;

    /** The Encoder Direction for the Strafe Encoder (Deadwheel)
     * Default Value: GoBildaPinpointDriver.EncoderDirection.FORWARD */
    public  GoBildaPinpointDriver.EncoderDirection strafeEncoderDirection = GoBildaPinpointDriver.EncoderDirection.FORWARD;

    private boolean forwardPodYInCurrentUnit = true;
    private boolean strafePodXInCurrentUnit = true;

    /**
     * This creates a new PinpointConstants with default values.
     */
    public PinpointConstants() {
        defaults();
    }

    public PinpointConstants forwardPodY(double forwardPodY) {
        this.forwardPodY = forwardPodY;
        this.forwardPodYInCurrentUnit = true;
        return this;
    }

    public PinpointConstants strafePodX(double strafePodX) {
        this.strafePodX = strafePodX;
        this.strafePodXInCurrentUnit = true;
        return this;
    }

    /**
     * Sets Pinpoint's hardware {@link DistanceUnit} without converting pod offsets.
     * After a unit change, set {@link #forwardPodY(double)} and {@link #strafePodX(double)}
     * in that unit, or use {@link #distanceUnit(DistanceUnit, boolean) distanceUnit(unit, true)}
     * to convert the previous measurements. {@link #validate()} / {@code FollowerBuilder.pinpointLocalizer}
     * reject a unit change that still has the inch defaults.
     */
    public PinpointConstants distanceUnit(DistanceUnit distanceUnit) {
        return distanceUnit(distanceUnit, false);
    }

    /**
     * Sets Pinpoint's hardware {@link DistanceUnit}.
     *
     * @param convertExistingMeasurements if true, convert pod offsets and a custom encoder
     *         resolution from the previous unit into {@code distanceUnit}
     */
    public PinpointConstants distanceUnit(DistanceUnit distanceUnit, boolean convertExistingMeasurements) {
        DistanceUnit next = HardwareLengths.requireHardwareUnit(distanceUnit);
        if (this.distanceUnit != next) {
            if (convertExistingMeasurements) {
                convertMeasurements(this.distanceUnit, next);
                forwardPodYInCurrentUnit = true;
                strafePodXInCurrentUnit = true;
            } else {
                forwardPodYInCurrentUnit = false;
                strafePodXInCurrentUnit = false;
            }
        }
        this.distanceUnit = next;
        return this;
    }

    /**
     * Convert pod offsets (and custom ticks-per-unit, if set) into the FTC unit that matches
     * TeamCode length. Pinpoint has no feet unit; feet stays inches.
     */
    public PinpointConstants alignDistanceUnit(PedroUnits units) {
        if (units == null) {
            throw new IllegalArgumentException("units must not be null");
        }
        return alignDistanceUnit(units.lengthUnit());
    }

    public PinpointConstants alignDistanceUnit(LengthUnit lengthUnit) {
        return distanceUnit(HardwareLengths.toDistanceUnit(lengthUnit), true);
    }

    /**
     * Checks that pod offsets match {@link #distanceUnit}. Called by
     * {@code FollowerBuilder.pinpointLocalizer}.
     */
    public void validate() {
        HardwareLengths.requireHardwareUnit(distanceUnit);
        boolean forwardReviewed = forwardPodYInCurrentUnit || forwardPodY != DEFAULT_FORWARD_POD_Y;
        boolean strafeReviewed = strafePodXInCurrentUnit || strafePodX != DEFAULT_STRAFE_POD_X;
        if (!forwardReviewed || !strafeReviewed) {
            throw new IllegalStateException(
                    "Pinpoint distanceUnit is "
                            + distanceUnit.name()
                            + ", but pod offsets were not set in that unit. Call forwardPodY(...) and "
                            + "strafePodX(...) with "
                            + distanceUnit.name()
                            + " measurements, or distanceUnit("
                            + distanceUnit.name()
                            + ", true) / alignDistanceUnit(...) to convert the previous values.");
        }
        checkOffsetMagnitude("forwardPodY", forwardPodY);
        checkOffsetMagnitude("strafePodX", strafePodX);
    }

    private void convertMeasurements(DistanceUnit from, DistanceUnit to) {
        forwardPodY = to.fromUnit(from, forwardPodY);
        strafePodX = to.fromUnit(from, strafePodX);
        if (customEncoderResolution.isPresent()) {
            double ticksPerOld = customEncoderResolution.getAsDouble();
            double oldUnitsPerNew = from.fromUnit(to, 1.0);
            customEncoderResolution = OptionalDouble.of(ticksPerOld * oldUnitsPerNew);
        }
    }

    private void checkOffsetMagnitude(String name, double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalStateException("Pinpoint " + name + " must be a finite number.");
        }
        double inches = Math.abs(HardwareLengths.toInches(value, distanceUnit));
        if (inches > MAX_POD_OFFSET_INCHES) {
            throw new IllegalStateException(
                    "Pinpoint "
                            + name
                            + " "
                            + value
                            + " "
                            + distanceUnit.name()
                            + " is "
                            + inches
                            + " inches from center, which is larger than a typical FTC robot. "
                            + "Offsets must be in "
                            + distanceUnit.name()
                            + ".");
        }
    }

    public PinpointConstants hardwareMapName(String hardwareMapName) {
        this.hardwareMapName = hardwareMapName;
        return this;
    }

    public PinpointConstants yawScalar(double yawScalar) {
        this.yawScalar = OptionalDouble.of(yawScalar);
        return this;
    }

    public PinpointConstants encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods encoderResolution) {
        this.encoderResolution = encoderResolution;
        return this;
    }

    public PinpointConstants customEncoderResolution(double customEncoderResolution) {
        this.customEncoderResolution = OptionalDouble.of(customEncoderResolution);
        return this;
    }

    public PinpointConstants forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection forwardEncoderDirection) {
        this.forwardEncoderDirection = forwardEncoderDirection;
        return this;
    }

    public PinpointConstants strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection strafeEncoderDirection) {
        this.strafeEncoderDirection = strafeEncoderDirection;
        return this;
    }

    public void defaults() {
        forwardPodY = DEFAULT_FORWARD_POD_Y;
        strafePodX = DEFAULT_STRAFE_POD_X;
        distanceUnit = DistanceUnit.INCH;
        hardwareMapName = "pinpoint";
        yawScalar = OptionalDouble.empty();
        encoderResolution = GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD;
        customEncoderResolution = OptionalDouble.empty();
        forwardEncoderDirection = GoBildaPinpointDriver.EncoderDirection.REVERSED;
        strafeEncoderDirection = GoBildaPinpointDriver.EncoderDirection.FORWARD;
        forwardPodYInCurrentUnit = true;
        strafePodXInCurrentUnit = true;
    }
}
