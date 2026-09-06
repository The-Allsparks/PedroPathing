package com.pedropathing.paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import com.pedropathing.math.LengthAnchors;
import com.pedropathing.math.LengthUnit;

import org.junit.Test;

public class PathConstraintsLengthUnitTest {
    private static final double EPS = 1e-12;

    @Test
    public void defaultConstraintsRemainInchAuthored() {
        assertEquals(LengthUnit.INCHES, PathConstraints.defaultConstraints.getLengthUnit());
        assertEquals(LengthAnchors.PATH_VELOCITY_CONSTRAINT, PathConstraints.defaultConstraints.getVelocityConstraint(), EPS);
        assertEquals(LengthAnchors.PATH_TRANSLATIONAL_CONSTRAINT, PathConstraints.defaultConstraints.getTranslationalConstraint(), EPS);
    }

    @Test
    public void shortConstructorStoresInchAnchorsWithoutLookingUpGlobalState() {
        PathConstraints constraints = new PathConstraints(0.99, 100, 1, 1);
        assertEquals(LengthUnit.INCHES, constraints.getLengthUnit());
        assertEquals(LengthAnchors.PATH_VELOCITY_CONSTRAINT, constraints.getVelocityConstraint(), EPS);
        PathConstraints cm = constraints.inUnit(LengthUnit.CENTIMETERS);
        assertEquals(LengthAnchors.PATH_VELOCITY_CONSTRAINT * 2.54, cm.getVelocityConstraint(), EPS);
        assertEquals(LengthAnchors.PATH_VELOCITY_CONSTRAINT, constraints.getVelocityConstraint(), EPS);
    }

    @Test
    public void fullConstructorPreservesValuesAlreadyInDeclaredUnit() {
        PathConstraints constraints = PathConstraints.inUnit(LengthUnit.CENTIMETERS, 0.99, 1.0, 2.0, 0.007, 100, 1, 10, 1);
        assertEquals(1.0, constraints.getVelocityConstraint(), EPS);
        assertEquals(2.0, constraints.getTranslationalConstraint(), EPS);
        PathConstraints again = constraints.inUnit(LengthUnit.CENTIMETERS);
        assertEquals(1.0, again.getVelocityConstraint(), EPS);
        assertEquals(2.0, again.getTranslationalConstraint(), EPS);
    }

    @Test
    public void fromInchesThenDefaultsForConvertsOnce() {
        PathConstraints cm = PathConstraints.defaultsFor(LengthUnit.CENTIMETERS);
        assertEquals(LengthAnchors.PATH_VELOCITY_CONSTRAINT * 2.54, cm.getVelocityConstraint(), EPS);
        PathConstraints again = cm.inUnit(LengthUnit.CENTIMETERS);
        assertEquals(cm.getVelocityConstraint(), again.getVelocityConstraint(), EPS);
        PathConstraints inches = cm.inUnit(LengthUnit.INCHES);
        assertEquals(LengthAnchors.PATH_VELOCITY_CONSTRAINT, inches.getVelocityConstraint(), EPS);
    }

    @Test
    public void copyingDoesNotConvertAgain() {
        PathConstraints cm = PathConstraints.defaultsFor(LengthUnit.FEET);
        PathConstraints copy = cm.copy();
        assertEquals(cm.getVelocityConstraint(), copy.getVelocityConstraint(), EPS);
        assertEquals(LengthUnit.FEET, copy.getLengthUnit());
        assertNotSame(cm, copy);
    }

    @Test
    public void inUnitDoesNotMutateSharedDefault() {
        double original = PathConstraints.defaultConstraints.getVelocityConstraint();
        PathConstraints converted = PathConstraints.defaultConstraints.inUnit(LengthUnit.METERS);
        assertEquals(original, PathConstraints.defaultConstraints.getVelocityConstraint(), EPS);
        assertSame(LengthUnit.INCHES, PathConstraints.defaultConstraints.getLengthUnit());
        assertEquals(LengthAnchors.PATH_VELOCITY_CONSTRAINT * 0.0254, converted.getVelocityConstraint(), EPS);
    }

    @Test
    public void settersAreInTheDeclaredUnit() {
        PathConstraints constraints = PathConstraints.defaultsFor(LengthUnit.CENTIMETERS);
        constraints.setVelocityConstraint(5.0);
        constraints.setTranslationalConstraint(6.0);
        assertEquals(5.0, constraints.inUnit(LengthUnit.CENTIMETERS).getVelocityConstraint(), EPS);
        assertEquals(6.0 / 2.54, constraints.inUnit(LengthUnit.INCHES).getTranslationalConstraint(), EPS);
    }
}
