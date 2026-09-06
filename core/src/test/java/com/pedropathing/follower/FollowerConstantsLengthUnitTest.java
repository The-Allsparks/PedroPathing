package com.pedropathing.follower;

import static org.junit.Assert.assertEquals;

import com.pedropathing.math.LengthAnchors;
import com.pedropathing.math.LengthUnit;
import com.pedropathing.paths.PathConstraints;

import org.junit.After;
import org.junit.Test;

public class FollowerConstantsLengthUnitTest {
    private static final double EPS = 1e-9;

    @After
    public void restoreInches() {
        LengthUnit.setActive(LengthUnit.INCHES);
    }

    @Test
    public void constructorFollowsActiveUnit() {
        LengthUnit.use(LengthUnit.CENTIMETERS);
        FollowerConstants constants = new FollowerConstants();

        assertEquals(LengthUnit.ofInches(LengthAnchors.DRIVE_PIDF_SWITCH), constants.drivePIDFSwitch, EPS);
        assertEquals(50.8, constants.drivePIDFSwitch, EPS);
        assertEquals(LengthUnit.ofInches(LengthAnchors.TRANSLATIONAL_PIDF_SWITCH), constants.translationalPIDFSwitch, EPS);
        assertEquals(LengthUnit.rescaleInverse(LengthAnchors.CENTRIPETAL_SCALING, LengthUnit.INCHES), constants.centripetalScaling, EPS);
        assertEquals(LengthUnit.rescaleSquared(LengthAnchors.KALMAN_MODEL_COVARIANCE, LengthUnit.INCHES), constants.driveKalmanFilterModelCovariance, EPS);
        assertEquals(LengthUnit.rescaleInverse(0.1, LengthUnit.INCHES), constants.coefficientsTranslationalPIDF.P, EPS);
        assertEquals(1.0, constants.coefficientsHeadingPIDF.P, EPS);
        assertEquals(LengthUnit.CENTIMETERS, constants.getLengthUnit());
    }

    @Test
    public void applyLengthUnitLooksUpActive() {
        FollowerConstants constants = new FollowerConstants();
        LengthUnit.setActive(LengthUnit.CENTIMETERS);
        constants.applyLengthUnit();
        double switchCm = constants.drivePIDFSwitch;
        assertEquals(50.8, switchCm, EPS);
        constants.applyLengthUnit();
        assertEquals(switchCm, constants.drivePIDFSwitch, EPS);
    }

    @Test
    public void roundTripBackToInches() {
        LengthUnit.setActive(LengthUnit.CENTIMETERS);
        FollowerConstants constants = new FollowerConstants();
        LengthUnit.setActive(LengthUnit.INCHES);
        constants.applyLengthUnit();
        assertEquals(LengthAnchors.DRIVE_PIDF_SWITCH, constants.drivePIDFSwitch, EPS);
        assertEquals(LengthAnchors.CENTRIPETAL_SCALING, constants.centripetalScaling, EPS);
        assertEquals(LengthAnchors.KALMAN_MODEL_COVARIANCE, constants.driveKalmanFilterModelCovariance, EPS);
    }

    @Test
    public void pathConstraintsConstructorFollowsActive() {
        LengthUnit.setActive(LengthUnit.CENTIMETERS);
        PathConstraints constraints = new PathConstraints(0.99, 100, 1, 1);
        assertEquals(LengthUnit.ofInches(LengthAnchors.PATH_VELOCITY_CONSTRAINT), constraints.getVelocityConstraint(), EPS);
        constraints.applyLengthUnit();
        assertEquals(LengthUnit.ofInches(LengthAnchors.PATH_VELOCITY_CONSTRAINT), constraints.getVelocityConstraint(), EPS);
        LengthUnit.setActive(LengthUnit.INCHES);
        constraints.applyLengthUnit();
        assertEquals(LengthAnchors.PATH_VELOCITY_CONSTRAINT, constraints.getVelocityConstraint(), EPS);
    }
}
