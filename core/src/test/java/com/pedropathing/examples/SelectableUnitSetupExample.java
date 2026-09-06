package com.pedropathing.examples;

import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.geometry.Pose;
import com.pedropathing.math.AngularUnit;
import com.pedropathing.math.LengthUnit;
import com.pedropathing.math.MassUnit;
import com.pedropathing.math.PedroUnits;
import com.pedropathing.paths.PathConstraints;

/**
 * Compile-and-assert fixture for the configured-interface unit API.
 */
public class SelectableUnitSetupExample {
    public static final PedroUnits UNITS = new PedroUnits(
            LengthUnit.CENTIMETERS,
            MassUnit.KILOGRAMS,
            AngularUnit.RADIANS);

    @org.junit.Test
    public void configuredPoseAndConstraintsConvertOnce() {
        FollowerConstants constants = new FollowerConstants().mass(10);
        org.junit.Assert.assertEquals(10.0, constants.mass, 1e-9);
        org.junit.Assert.assertEquals(0.1, constants.coefficientsTranslationalPIDF.P, 1e-9);

        Pose start = UNITS.pose(0, 0, 0);
        Pose end = UNITS.pose(60.96, 0, 0);
        org.junit.Assert.assertEquals(0.0, start.getX(), 1e-9);
        org.junit.Assert.assertEquals(24.0, end.getX(), 1e-9);

        PathConstraints constraints = UNITS.pathConstraints()
                .velocityConstraint(2.54)
                .translationalConstraint(2.54)
                .build();
        org.junit.Assert.assertEquals(1.0, constraints.getVelocityConstraint(), 1e-9);
        org.junit.Assert.assertEquals(0.1, PathConstraints.defaultConstraints.getVelocityConstraint(), 1e-9);
    }
}
