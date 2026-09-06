package com.pedropathing.ftc.drivetrains;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import com.pedropathing.math.LengthAnchors;
import com.pedropathing.math.LengthUnit;
import com.pedropathing.paths.PathConstraints;
import com.pedropathing.follower.FollowerConstants;

import org.junit.Test;

public class ConstantsCopyTest {
    private static final double EPS = 1e-9;

    @Test
    public void mecanumInUnitDoesNotMutateCaller() {
        MecanumConstants original = new MecanumConstants();
        double x = original.xVelocity;
        MecanumConstants cm = original.inUnit(LengthUnit.CENTIMETERS);
        assertEquals(x, original.xVelocity, EPS);
        assertEquals(LengthUnit.INCHES, original.getLengthUnit());
        assertNotSame(original, cm);
        assertEquals(x * 2.54, cm.xVelocity, EPS);
        assertEquals(LengthAnchors.MECANUM_X_VELOCITY * 2.54, MecanumConstants.defaultsFor(LengthUnit.CENTIMETERS).xVelocity, EPS);
    }

    @Test
    public void swerveInUnitDoesNotMutateCaller() {
        SwerveConstants original = new SwerveConstants();
        double x = original.xVelocity;
        SwerveConstants feet = original.inUnit(LengthUnit.FEET);
        assertEquals(x, original.xVelocity, EPS);
        assertEquals(x / 12.0, feet.xVelocity, EPS);
    }

    @Test
    public void followerAndPathCopiesDoNotMutateCallers() {
        FollowerConstants constants = new FollowerConstants();
        PathConstraints constraints = new PathConstraints(0.99, 100, 1, 1);
        double switchInches = constants.drivePIDFSwitch;
        double velocity = constraints.getVelocityConstraint();
        constants.inUnit(LengthUnit.METERS);
        constraints.inUnit(LengthUnit.METERS);
        assertEquals(switchInches, constants.drivePIDFSwitch, EPS);
        assertEquals(velocity, constraints.getVelocityConstraint(), EPS);
    }

    @Test
    public void derivedMecanumVectorUpdatesWhenVelocityChanges() {
        MecanumConstants constants = new MecanumConstants();
        double originalTheta = constants.frontLeftVector.getTheta();
        constants.xVelocity(10).yVelocity(0);
        assertEquals(0.0, constants.frontLeftVector.getTheta(), 1e-9);
        constants.xVelocity(LengthAnchors.MECANUM_X_VELOCITY).yVelocity(LengthAnchors.MECANUM_Y_VELOCITY);
        assertEquals(originalTheta, constants.frontLeftVector.getTheta(), 1e-6);
    }
}
