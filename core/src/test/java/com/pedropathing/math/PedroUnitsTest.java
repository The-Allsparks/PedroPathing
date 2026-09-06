package com.pedropathing.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathConstraints;

import org.junit.Test;

public class PedroUnitsTest {
    private static final double EPS = 1e-9;

    @Test
    public void defaultUnitsMatchUpstream() {
        assertEquals(LengthUnit.INCHES, PedroUnits.DEFAULT.lengthUnit());
        assertEquals(MassUnit.KILOGRAMS, PedroUnits.DEFAULT.massUnit());
        assertEquals(AngularUnit.RADIANS, PedroUnits.DEFAULT.angleUnit());
    }

    @Test
    public void lengthConversions() {
        PedroUnits feet = new PedroUnits(LengthUnit.FEET, MassUnit.KILOGRAMS, AngularUnit.RADIANS);
        assertEquals(24.0, feet.lengthToInternal(2.0), EPS);
        assertEquals(2.0, feet.lengthFromInternal(24.0), EPS);

        PedroUnits cm = new PedroUnits(LengthUnit.CENTIMETERS, MassUnit.KILOGRAMS, AngularUnit.RADIANS);
        assertEquals(1.0, cm.lengthToInternal(2.54), EPS);
        assertEquals(2.54, cm.lengthFromInternal(1.0), EPS);

        PedroUnits meters = new PedroUnits(LengthUnit.METERS, MassUnit.KILOGRAMS, AngularUnit.RADIANS);
        assertEquals(1.0, meters.lengthToInternal(0.0254), EPS);
        assertEquals(0.0254, meters.lengthFromInternal(1.0), EPS);
    }

    @Test
    public void identityLengthConversion() {
        assertEquals(12.0, PedroUnits.DEFAULT.lengthToInternal(12.0), EPS);
        assertEquals(12.0, PedroUnits.DEFAULT.lengthFromInternal(12.0), EPS);
    }

    @Test
    public void velocityAndAccelerationUseLengthScale() {
        PedroUnits feet = new PedroUnits(LengthUnit.FEET, MassUnit.KILOGRAMS, AngularUnit.RADIANS);
        assertEquals(6.0, feet.velocityToInternal(0.5), EPS);
        assertEquals(0.5, feet.velocityFromInternal(6.0), EPS);
        assertEquals(-12.0, feet.accelerationToInternal(-1.0), EPS);
        assertEquals(-1.0, feet.accelerationFromInternal(-12.0), EPS);
    }

    @Test
    public void massConversions() {
        PedroUnits pounds = new PedroUnits(LengthUnit.INCHES, MassUnit.POUNDS, AngularUnit.RADIANS);
        assertEquals(10.659420695, pounds.massToInternal(23.5), 1e-9);
        assertEquals(23.5, pounds.massFromInternal(10.659420695), 1e-9);
        assertEquals(10.65, PedroUnits.DEFAULT.massToInternal(10.65), EPS);
        assertEquals(10.65, PedroUnits.DEFAULT.massFromInternal(10.65), EPS);
    }

    @Test
    public void angleConversions() {
        PedroUnits degrees = new PedroUnits(LengthUnit.INCHES, MassUnit.KILOGRAMS, AngularUnit.DEGREES);
        assertEquals(Math.PI / 2, degrees.angleToInternal(90), EPS);
        assertEquals(90.0, degrees.angleFromInternal(Math.PI / 2), EPS);
        assertEquals(Math.PI / 2, PedroUnits.DEFAULT.angleToInternal(Math.PI / 2), EPS);
    }

    @Test
    public void poseFactoryStoresConfiguredUnits() {
        PedroUnits units = new PedroUnits(LengthUnit.FEET, MassUnit.POUNDS, AngularUnit.DEGREES);
        Pose pose = units.pose(2, 4, 90);
        assertEquals(2.0, pose.getX(), EPS);
        assertEquals(4.0, pose.getY(), EPS);
        assertEquals(90.0, pose.getHeading(), EPS);
        Pose internal = units.toInternalPose(pose);
        assertEquals(24.0, internal.getX(), EPS);
        assertEquals(48.0, internal.getY(), EPS);
        assertEquals(Math.PI / 2, internal.getHeading(), EPS);
        Pose roundTrip = units.toUserPose(internal);
        assertEquals(2.0, roundTrip.getX(), EPS);
        assertEquals(90.0, roundTrip.getHeading(), EPS);
    }

    @Test
    public void configuredOutputDoesNotReturnPose() {
        PedroUnits units = new PedroUnits(LengthUnit.CENTIMETERS, MassUnit.KILOGRAMS, AngularUnit.DEGREES);
        Pose internal = new Pose(24, 48, Math.PI / 2);
        ConfiguredPose display = units.fromInternalPose(internal);
        assertEquals(60.96, display.x(), EPS);
        assertEquals(121.92, display.y(), EPS);
        assertEquals(90.0, display.heading(), EPS);
        assertEquals(LengthUnit.CENTIMETERS, display.lengthUnit());
        assertTrue(display.toString().contains("cm"));
        Pose roundTrip = display.toInternalPose();
        assertEquals(24.0, roundTrip.getX(), EPS);
        assertEquals(Math.PI / 2, roundTrip.getHeading(), EPS);
    }

    @Test
    public void pathConstraintsBuilderConvertsOnceAndDoesNotMutateDefaults() {
        PedroUnits units = new PedroUnits(LengthUnit.FEET, MassUnit.KILOGRAMS, AngularUnit.DEGREES);
        double defaultVelocity = PathConstraints.defaultConstraints.getVelocityConstraint();
        PathConstraints constraints = units.pathConstraints()
                .velocityConstraint(0.5)
                .translationalConstraint(0.05)
                .headingConstraint(2)
                .build();
        assertEquals(6.0, constraints.getVelocityConstraint(), EPS);
        assertEquals(0.6, constraints.getTranslationalConstraint(), EPS);
        assertEquals(Math.toRadians(2), constraints.getHeadingConstraint(), EPS);
        assertEquals(defaultVelocity, PathConstraints.defaultConstraints.getVelocityConstraint(), EPS);
        assertNotSame(PathConstraints.defaultConstraints, constraints);
    }

    @Test
    public void nullUnitsAreRejected() {
        try {
            new PedroUnits(null, MassUnit.KILOGRAMS, AngularUnit.RADIANS);
            fail();
        } catch (IllegalArgumentException expected) {
            // expected
        }
        try {
            new PedroUnits(LengthUnit.INCHES, null, AngularUnit.RADIANS);
            fail();
        } catch (IllegalArgumentException expected) {
            // expected
        }
        try {
            new PedroUnits(LengthUnit.INCHES, MassUnit.KILOGRAMS, null);
            fail();
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void equalityHashCodeAndImmutability() {
        PedroUnits a = new PedroUnits(LengthUnit.FEET, MassUnit.POUNDS, AngularUnit.DEGREES);
        PedroUnits b = new PedroUnits(LengthUnit.FEET, MassUnit.POUNDS, AngularUnit.DEGREES);
        PedroUnits c = new PedroUnits(LengthUnit.METERS, MassUnit.POUNDS, AngularUnit.DEGREES);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotSame(a, b);
        assertTrue(a.toString().contains("feet"));
    }

    @Test
    public void fieldHelpersUseConfiguredLength() {
        PedroUnits feet = new PedroUnits(LengthUnit.FEET, MassUnit.KILOGRAMS, AngularUnit.RADIANS);
        assertEquals(2.0, feet.tile(), EPS);
        assertEquals(12.0, feet.fieldSize(), EPS);
        assertEquals(6.0, feet.fieldCenter(), EPS);
        assertEquals(141.5 / 12.0, feet.mirrorFieldLength(), EPS);
    }
}
