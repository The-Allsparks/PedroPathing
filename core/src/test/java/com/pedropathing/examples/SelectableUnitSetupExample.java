package com.pedropathing.examples;

import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.math.LengthUnit;
import com.pedropathing.paths.PathConstraints;

import org.junit.Test;

/**
 * Representative upstream-style TeamCode constants that compile against the unit-aware APIs.
 */
public class SelectableUnitSetupExample {
    public static final LengthUnit LENGTH = LengthUnit.CENTIMETERS;

    public static FollowerConstants followerConstants =
            FollowerConstants.defaultsFor(LENGTH)
                    .mass(10)
                    .translationalPIDFCoefficients(new PIDFCoefficients(0.1 / 2.54, 0, 0, 0));

    public static PathConstraints pathConstraints = new PathConstraints(0.99, 100, 1, 1);

    @Test
    public void inchShortConstructorCanBeConvertedAtFollowerBoundary() {
        PathConstraints converted = pathConstraints.inUnit(LENGTH);
        org.junit.Assert.assertEquals(LengthUnit.CENTIMETERS, converted.getLengthUnit());
        org.junit.Assert.assertEquals(LengthUnit.INCHES, pathConstraints.getLengthUnit());
    }

    @Test
    public void fluentDeprecatedStyleStillCompiles() {
        FollowerConstants inch = new FollowerConstants().mass(10.65);
        org.junit.Assert.assertEquals(LengthUnit.INCHES, inch.getLengthUnit());
    }
}
