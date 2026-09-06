package com.pedropathing.follower;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

public class FollowerConstantsCanonicalTest {
    private static final double EPS = 1e-12;

    @Test
    public void constructedDefaultsMatchUpstreamInchesKilogramsRadians() {
        FollowerConstants constants = new FollowerConstants();
        assertEquals(20.0, constants.drivePIDFSwitch, EPS);
        assertEquals(3.0, constants.translationalPIDFSwitch, EPS);
        assertEquals(0.0005, constants.centripetalScaling, EPS);
        assertEquals(6.0, constants.driveKalmanFilterModelCovariance, EPS);
        assertEquals(1.0, constants.driveKalmanFilterDataCovariance, EPS);
        assertEquals(1.0, constants.stuckVelocity, EPS);
        assertEquals(-41.278, constants.forwardZeroPowerAcceleration, EPS);
        assertEquals(-59.7819, constants.lateralZeroPowerAcceleration, EPS);
        assertEquals(10.65, constants.mass, EPS);
        assertEquals(0.1, constants.coefficientsTranslationalPIDF.P, EPS);
        assertEquals(0.025, constants.coefficientsDrivePIDF.P, EPS);
        assertEquals(0.15, constants.predictiveBrakingCoefficients.P, EPS);
        assertEquals(Math.PI / 20, constants.headingPIDFSwitch, EPS);
    }

    @Test
    public void copyDoesNotMutateOriginalOrRescalePid() {
        FollowerConstants original = new FollowerConstants().mass(12.0);
        original.coefficientsTranslationalPIDF.P = 0.2;
        FollowerConstants copy = original.copy();
        copy.mass(99.0);
        copy.coefficientsTranslationalPIDF.P = 9.0;
        assertEquals(12.0, original.mass, EPS);
        assertEquals(0.2, original.coefficientsTranslationalPIDF.P, EPS);
        assertEquals(99.0, copy.mass, EPS);
        assertNotSame(original.coefficientsTranslationalPIDF, copy.coefficientsTranslationalPIDF);
    }
}
