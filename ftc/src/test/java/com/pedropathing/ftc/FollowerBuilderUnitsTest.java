package com.pedropathing.ftc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import com.pedropathing.drivetrain.Drivetrain;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.geometry.Pose;
import com.pedropathing.localization.Localizer;
import com.pedropathing.math.AngularUnit;
import com.pedropathing.math.LengthUnit;
import com.pedropathing.math.MassUnit;
import com.pedropathing.math.PedroUnits;
import com.pedropathing.math.Vector;
import com.pedropathing.paths.PathConstraints;

import org.junit.Test;

public class FollowerBuilderUnitsTest {
    private static final double EPS = 1e-9;

    @Test
    public void defaultBuilderPreservesCallerConstantsAndDefaultUnits() {
        FollowerConstants constants = new FollowerConstants().mass(10.65);
        FollowerBuilder builder = new FollowerBuilder(constants, null);
        assertSame(PedroUnits.DEFAULT, builder.getUnits());
        Follower follower = builder
                .setLocalizer(new FakeLocalizer())
                .setDrivetrain(new FakeDrivetrain())
                .build();
        assertSame(constants, follower.getConstants());
        assertEquals(10.65, follower.getConstants().mass, EPS);
        assertSame(PedroUnits.DEFAULT, follower.getUnits());
    }

    @Test
    public void setUnitsConvertsConfiguredInputsOnce() {
        FollowerConstants constants = new FollowerConstants().mass(10.65);
        FollowerBuilder builder = new FollowerBuilder(constants, null)
                .setUnits(LengthUnit.FEET, MassUnit.POUNDS, AngularUnit.DEGREES)
                .setMass(23.5)
                .setStartingPose(2, 4, 90)
                .setPathCompletionVelocity(0.5)
                .setPathCompletionTranslationalTolerance(0.05)
                .setForwardZeroPowerAcceleration(-3.0);
        Follower follower = builder
                .setLocalizer(new FakeLocalizer())
                .setDrivetrain(new FakeDrivetrain())
                .build();
        assertEquals(10.65, constants.mass, EPS);
        assertEquals(-41.278, constants.forwardZeroPowerAcceleration, 1e-6);
        assertNotSame(constants, follower.getConstants());
        assertEquals(10.659420695, follower.getConstants().mass, 1e-9);
        assertEquals(-36.0, follower.getConstants().forwardZeroPowerAcceleration, EPS);
        assertEquals(6.0, follower.getConstraints().getVelocityConstraint(), EPS);
        assertEquals(0.6, follower.getConstraints().getTranslationalConstraint(), EPS);
        Pose start = follower.pose(2, 4, 90);
        assertEquals(24.0, start.getX(), EPS);
        assertEquals(48.0, start.getY(), EPS);
        assertEquals(Math.PI / 2, start.getHeading(), EPS);
        assertEquals(0.1, PathConstraints.defaultConstraints.getVelocityConstraint(), EPS);
    }

    @Test
    public void twoBuildersDoNotShareUnitState() {
        FollowerBuilder feet = new FollowerBuilder(new FollowerConstants(), null)
                .setUnits(LengthUnit.FEET);
        FollowerBuilder cm = new FollowerBuilder(new FollowerConstants(), null)
                .setUnits(LengthUnit.CENTIMETERS);
        assertEquals(LengthUnit.FEET, feet.getUnits().lengthUnit());
        assertEquals(LengthUnit.CENTIMETERS, cm.getUnits().lengthUnit());
    }

    @Test
    public void setUnitsTwiceThrows() {
        FollowerBuilder builder = new FollowerBuilder(new FollowerConstants(), null)
                .setUnits(PedroUnits.DEFAULT);
        try {
            builder.setUnits(LengthUnit.FEET);
            fail();
        } catch (IllegalStateException expected) {
            // expected
        }
    }

    @Test
    public void setUnitsAfterConfiguredSetterThrows() {
        FollowerBuilder builder = new FollowerBuilder(new FollowerConstants(), null)
                .setMass(10);
        try {
            builder.setUnits(LengthUnit.FEET);
            fail();
        } catch (IllegalStateException expected) {
            // expected
        }
    }

    @Test
    public void drivetrainVelocityOverrideDoesNotMutateCallerConstants() {
        MecanumConstants mecanum = new MecanumConstants();
        double originalX = mecanum.xVelocity;
        FollowerBuilder builder = new FollowerBuilder(new FollowerConstants(), null)
                .setUnits(LengthUnit.FEET)
                .setXVelocity(2.0);
        MecanumConstants copy = mecanum.copy().xVelocity(builder.getUnits().velocityToInternal(2.0));
        assertEquals(24.0, copy.xVelocity, EPS);
        assertEquals(originalX, mecanum.xVelocity, EPS);
    }

    @Test
    public void buildingTwiceDoesNotRescaleConstants() {
        FollowerConstants constants = new FollowerConstants();
        FollowerBuilder builder = new FollowerBuilder(constants, null)
                .setUnits(LengthUnit.FEET, MassUnit.POUNDS, AngularUnit.DEGREES)
                .setMass(23.5)
                .setLocalizer(new FakeLocalizer())
                .setDrivetrain(new FakeDrivetrain());
        Follower first = builder.build();
        Follower second = builder.build();
        assertEquals(first.getConstants().mass, second.getConstants().mass, EPS);
        assertEquals(10.659420695, first.getConstants().mass, 1e-9);
        assertEquals(0.1, first.getConstants().coefficientsTranslationalPIDF.P, EPS);
        assertEquals(0.1, second.getConstants().coefficientsTranslationalPIDF.P, EPS);
    }

    private static final class FakeLocalizer implements Localizer {
        private Pose pose = new Pose();

        @Override public Pose getPose() { return pose; }
        @Override public Pose getVelocity() { return new Pose(); }
        @Override public Vector getVelocityVector() { return new Vector(); }
        @Override public void setStartPose(Pose setStart) { pose = setStart; }
        @Override public void setPose(Pose setPose) { pose = setPose; }
        @Override public void update() {}
        @Override public double getTotalHeading() { return 0; }
        @Override public double getForwardMultiplier() { return 1; }
        @Override public double getLateralMultiplier() { return 1; }
        @Override public double getTurningMultiplier() { return 1; }
        @Override public void resetIMU() {}
        @Override public double getIMUHeading() { return Double.NaN; }
        @Override public boolean isNAN() { return false; }
    }

    private static final class FakeDrivetrain extends Drivetrain {
        @Override public double[] calculateDrive(Vector correctivePower, Vector headingPower, Vector pathingPower, double robotHeading) { return new double[4]; }
        @Override public void updateConstants() {}
        @Override public void breakFollowing() {}
        @Override public void runDrive(double[] drivePowers) {}
        @Override public void startTeleopDrive() {}
        @Override public void startTeleopDrive(boolean brakeMode) {}
        @Override public double xVelocity() { return 0; }
        @Override public double yVelocity() { return 0; }
        @Override public void setXVelocity(double xMovement) {}
        @Override public void setYVelocity(double yMovement) {}
        @Override public double getVoltage() { return 12; }
        @Override public String debugString() { return "fake"; }
    }
}
