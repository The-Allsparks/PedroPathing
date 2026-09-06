package com.pedropathing.paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

public class PathConstraintsCanonicalTest {
    private static final double EPS = 1e-12;

    @Test
    public void defaultsMatchUpstreamInchesAndRadians() {
        PathConstraints constraints = PathConstraints.defaultConstraints;
        assertEquals(0.995, constraints.getTValueConstraint(), EPS);
        assertEquals(0.1, constraints.getVelocityConstraint(), EPS);
        assertEquals(0.1, constraints.getTranslationalConstraint(), EPS);
        assertEquals(0.007, constraints.getHeadingConstraint(), EPS);
        assertEquals(100.0, constraints.getTimeoutConstraint(), EPS);
    }

    @Test
    public void copyDoesNotConvertOrMutateSharedDefault() {
        PathConstraints copy = PathConstraints.defaultConstraints.copy();
        copy.setVelocityConstraint(5.0);
        assertEquals(0.1, PathConstraints.defaultConstraints.getVelocityConstraint(), EPS);
        assertEquals(5.0, copy.getVelocityConstraint(), EPS);
        assertNotSame(PathConstraints.defaultConstraints, copy);
    }

    @Test
    public void inchConstructorStoresCallerValuesAsCanonical() {
        PathConstraints constraints = new PathConstraints(0.99, 100, 1, 1);
        assertEquals(0.1, constraints.getVelocityConstraint(), EPS);
        assertEquals(0.1, constraints.getTranslationalConstraint(), EPS);
        assertEquals(0.007, constraints.getHeadingConstraint(), EPS);
    }
}
