package com.pedropathing.follower;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.math.AngularUnit;
import com.pedropathing.math.ConfiguredPose;
import com.pedropathing.math.LengthUnit;
import com.pedropathing.math.MassUnit;
import com.pedropathing.math.PedroUnits;
import com.pedropathing.paths.PathChain;
import com.pedropathing.paths.PathConstraints;
import com.pedropathing.support.TestDrivetrain;
import com.pedropathing.support.TestLocalizer;

import org.junit.Test;

public class FollowerUnitsTest {
    private static final double EPS = 1e-9;

    @Test
    public void defaultConstructorKeepsCanonicalUnitsAndDefaults() {
        FollowerConstants constants = new FollowerConstants();
        Follower follower = new Follower(constants, new TestLocalizer(), new TestDrivetrain());
        assertSame(PedroUnits.DEFAULT, follower.getUnits());
        assertEquals(10.65, follower.getConstants().mass, EPS);
        assertEquals(0.1, follower.getConstraints().getVelocityConstraint(), EPS);
        Pose pose = follower.pose(24, 48, Math.PI / 2);
        assertEquals(24.0, pose.getX(), EPS);
        assertEquals(Math.PI / 2, pose.getHeading(), EPS);
        assertEquals(24.0, follower.getUnits().toInternalPose(pose).getX(), EPS);
    }

    @Test
    public void configuredPoseFactoryAndOutputStayInSelectedUnits() {
        PedroUnits units = new PedroUnits(LengthUnit.FEET, MassUnit.POUNDS, AngularUnit.DEGREES);
        TestLocalizer localizer = new TestLocalizer();
        Follower follower = new Follower(
                new FollowerConstants(),
                localizer,
                new TestDrivetrain(),
                PathConstraints.defaultConstraints,
                units);
        Pose start = follower.pose(2, 4, 90);
        assertEquals(2.0, start.getX(), EPS);
        assertEquals(4.0, start.getY(), EPS);
        assertEquals(90.0, start.getHeading(), EPS);

        follower.setStartingPose(start);
        assertEquals(2.0, follower.getPose().getX(), EPS);
        assertEquals(4.0, follower.getPose().getY(), EPS);
        assertEquals(90.0, follower.getPose().getHeading(), EPS);
        assertEquals(24.0, follower.getInternalPose().getX(), EPS);
        assertEquals(48.0, follower.getInternalPose().getY(), EPS);
        assertEquals(Math.PI / 2, follower.getInternalPose().getHeading(), EPS);

        ConfiguredPose display = follower.getPoseInConfiguredUnits();
        assertEquals(2.0, display.x(), EPS);
        assertEquals(4.0, display.y(), EPS);
        assertEquals(90.0, display.heading(), EPS);
        assertEquals("ft", display.lengthUnit().symbol());
        assertEquals("deg", display.angleUnit().symbol());
    }

    @Test
    public void twoFollowersCanUseDifferentInterfaceUnits() {
        PedroUnits feet = new PedroUnits(LengthUnit.FEET, MassUnit.KILOGRAMS, AngularUnit.DEGREES);
        PedroUnits cm = new PedroUnits(LengthUnit.CENTIMETERS, MassUnit.KILOGRAMS, AngularUnit.DEGREES);
        Follower a = new Follower(new FollowerConstants(), new TestLocalizer(), new TestDrivetrain(), PathConstraints.defaultConstraints, feet);
        Follower b = new Follower(new FollowerConstants(), new TestLocalizer(), new TestDrivetrain(), PathConstraints.defaultConstraints, cm);
        assertEquals(2.0, a.pose(2, 0, 0).getX(), EPS);
        assertEquals(60.96, b.pose(60.96, 0, 0).getX(), EPS);
        assertEquals(24.0, a.getUnits().toInternalPose(a.pose(2, 0, 0)).getX(), EPS);
        assertEquals(24.0, b.getUnits().toInternalPose(b.pose(60.96, 0, 0)).getX(), EPS);
        assertNotSame(a.getUnits(), b.getUnits());
        assertEquals(20.0, a.getConstants().drivePIDFSwitch, EPS);
        assertEquals(20.0, b.getConstants().drivePIDFSwitch, EPS);
    }

    @Test
    public void pathBuilderConvertsConfiguredPosesOnce() {
        PedroUnits units = new PedroUnits(LengthUnit.FEET, MassUnit.KILOGRAMS, AngularUnit.DEGREES);
        Follower follower = new Follower(
                new FollowerConstants(),
                new TestLocalizer(),
                new TestDrivetrain(),
                PathConstraints.defaultConstraints,
                units);
        PathChain chain = follower.pathBuilder()
                .addPath(new BezierLine(follower.pose(2, 0, 0), follower.pose(4, 0, 90)))
                .setConstantHeadingInterpolation(90)
                .build();
        assertEquals(24.0, chain.getPath(0).getFirstControlPoint().getX(), EPS);
        assertEquals(48.0, chain.getPath(0).getLastControlPoint().getX(), EPS);
        assertEquals(Math.PI / 2, chain.getPath(0).getHeadingGoal(1.0), EPS);
    }

    @Test
    public void setConstraintsDoesNotRescale() {
        Follower follower = new Follower(new FollowerConstants(), new TestLocalizer(), new TestDrivetrain());
        PathConstraints custom = new PathConstraints(0.99, 0.5, 0.25, 0.02, 50, 1, 10, 1);
        follower.setConstraints(custom);
        assertEquals(0.5, follower.getConstraints().getVelocityConstraint(), EPS);
        assertEquals(0.25, follower.getConstraints().getTranslationalConstraint(), EPS);
    }
}
