package com.pedropathing.follower;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import com.pedropathing.math.LengthAnchors;
import com.pedropathing.math.LengthUnit;

import org.junit.Test;

public class FollowerConstantsLengthUnitTest {
    private static final double EPS = 1e-9;

    @Test
    public void constructorRemainsInchAuthored() {
        FollowerConstants constants = new FollowerConstants();
        assertEquals(LengthUnit.INCHES, constants.getLengthUnit());
        assertEquals(LengthAnchors.DRIVE_PIDF_SWITCH, constants.drivePIDFSwitch, EPS);
        assertEquals(LengthAnchors.CENTRIPETAL_SCALING, constants.centripetalScaling, EPS);
        assertEquals(LengthAnchors.KALMAN_MODEL_COVARIANCE, constants.driveKalmanFilterModelCovariance, EPS);
        assertEquals(0.1, constants.coefficientsTranslationalPIDF.P, EPS);
        assertEquals(0.015, constants.integralTranslational.F, EPS);
        assertEquals(1.0, constants.coefficientsHeadingPIDF.P, EPS);
        assertEquals(0.6, constants.coefficientsDrivePIDF.T, EPS);
        assertEquals(LengthAnchors.PREDICTIVE_BRAKING_P, constants.predictiveBrakingCoefficients.P, EPS);
        assertEquals(LengthAnchors.PREDICTIVE_BRAKING_LINEAR, constants.predictiveBrakingCoefficients.kLinearBraking, EPS);
    }

    @Test
    public void defaultsForEachUnitArePhysicallyEquivalent() {
        FollowerConstants inches = FollowerConstants.defaultsFor(LengthUnit.INCHES);
        for (LengthUnit unit : LengthUnit.values()) {
            FollowerConstants converted = FollowerConstants.defaultsFor(unit);
            assertEquals(unit, converted.getLengthUnit());
            assertEquals(LengthAnchors.DRIVE_PIDF_SWITCH, unit.toInches(converted.drivePIDFSwitch), EPS);
            assertEquals(LengthAnchors.TRANSLATIONAL_PIDF_SWITCH, unit.toInches(converted.translationalPIDFSwitch), EPS);
            assertEquals(LengthAnchors.STUCK_VELOCITY, unit.toInches(converted.stuckVelocity), EPS);
            assertEquals(LengthAnchors.FORWARD_ZERO_POWER_ACCELERATION, unit.toInches(converted.forwardZeroPowerAcceleration), EPS);
            assertEquals(LengthAnchors.LATERAL_ZERO_POWER_ACCELERATION, unit.toInches(converted.lateralZeroPowerAcceleration), EPS);
            assertEquals(LengthAnchors.CENTRIPETAL_SCALING, LengthUnit.rescaleInverse(converted.centripetalScaling, unit, LengthUnit.INCHES), EPS);
            assertEquals(LengthAnchors.KALMAN_MODEL_COVARIANCE, LengthUnit.rescaleSquared(converted.driveKalmanFilterModelCovariance, unit, LengthUnit.INCHES), EPS);
            assertEquals(LengthAnchors.KALMAN_DATA_COVARIANCE, LengthUnit.rescaleSquared(converted.driveKalmanFilterDataCovariance, unit, LengthUnit.INCHES), EPS);
            assertEquals(0.1, LengthUnit.rescaleInverse(converted.coefficientsTranslationalPIDF.P, unit, LengthUnit.INCHES), EPS);
            assertEquals(0.025, LengthUnit.rescaleInverse(converted.coefficientsDrivePIDF.P, unit, LengthUnit.INCHES), EPS);
            assertEquals(inches.coefficientsDrivePIDF.F, converted.coefficientsDrivePIDF.F, EPS);
            assertEquals(inches.coefficientsDrivePIDF.T, converted.coefficientsDrivePIDF.T, EPS);
            assertEquals(inches.coefficientsHeadingPIDF.P, converted.coefficientsHeadingPIDF.P, EPS);
            assertEquals(inches.mass, converted.mass, EPS);
            assertEquals(LengthAnchors.PREDICTIVE_BRAKING_P, LengthUnit.rescaleInverse(converted.predictiveBrakingCoefficients.P, unit, LengthUnit.INCHES), EPS);
            assertEquals(LengthAnchors.PREDICTIVE_BRAKING_LINEAR, converted.predictiveBrakingCoefficients.kLinearBraking, EPS);
            assertEquals(LengthAnchors.PREDICTIVE_BRAKING_QUADRATIC, LengthUnit.rescaleInverse(converted.predictiveBrakingCoefficients.kQuadraticFriction, unit, LengthUnit.INCHES), EPS);
        }
    }

    @Test
    public void inUnitDoesNotMutateOriginalOrCompound() {
        FollowerConstants inches = new FollowerConstants();
        inches.forwardZeroPowerAcceleration(-50);
        FollowerConstants centimeters = inches.inUnit(LengthUnit.CENTIMETERS);
        assertEquals(-50.0, inches.forwardZeroPowerAcceleration, EPS);
        assertEquals(LengthUnit.INCHES, inches.getLengthUnit());
        assertNotSame(inches, centimeters);
        assertEquals(-50.0 * 2.54, centimeters.forwardZeroPowerAcceleration, EPS);
        FollowerConstants again = centimeters.inUnit(LengthUnit.CENTIMETERS);
        assertEquals(centimeters.forwardZeroPowerAcceleration, again.forwardZeroPowerAcceleration, EPS);
        FollowerConstants back = centimeters.inUnit(LengthUnit.INCHES);
        assertEquals(-50.0, back.forwardZeroPowerAcceleration, EPS);
    }

    @Test
    public void twoConfigurationsDoNotLeakState() {
        FollowerConstants cm = FollowerConstants.defaultsFor(LengthUnit.CENTIMETERS);
        FollowerConstants ft = FollowerConstants.defaultsFor(LengthUnit.FEET);
        assertEquals(50.8, cm.drivePIDFSwitch, EPS);
        assertEquals(LengthAnchors.DRIVE_PIDF_SWITCH / 12.0, ft.drivePIDFSwitch, EPS);
        assertEquals(50.8, cm.drivePIDFSwitch, EPS);
    }

    @Test
    public void constructionOrderDoesNotAffectInchDefaults() {
        FollowerConstants.defaultsFor(LengthUnit.METERS);
        FollowerConstants inches = new FollowerConstants();
        assertEquals(LengthAnchors.DRIVE_PIDF_SWITCH, inches.drivePIDFSwitch, EPS);
    }

    @Test
    public void copyDoesNotConvertAgain() {
        FollowerConstants cm = FollowerConstants.defaultsFor(LengthUnit.CENTIMETERS);
        double switchCm = cm.drivePIDFSwitch;
        FollowerConstants copy = cm.copy();
        assertEquals(switchCm, copy.drivePIDFSwitch, EPS);
        assertEquals(LengthUnit.CENTIMETERS, copy.getLengthUnit());
        copy.coefficientsTranslationalPIDF.P = 99;
        assertEquals(LengthUnit.rescaleInverse(0.1, LengthUnit.INCHES, LengthUnit.CENTIMETERS), cm.coefficientsTranslationalPIDF.P, EPS);
    }
}
