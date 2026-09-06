package com.pedropathing.ftc.drivetrains;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

public class ConstantsCopyTest {
    private static final double EPS = 1e-12;

    @Test
    public void mecanumCopyDoesNotMutateOriginalAndRecomputesVector() {
        MecanumConstants original = new MecanumConstants();
        double originalX = original.xVelocity;
        MecanumConstants copy = original.copy();
        copy.xVelocity(24.0);
        assertEquals(originalX, original.xVelocity, EPS);
        assertEquals(24.0, copy.xVelocity, EPS);
        assertNotSame(original, copy);
        assertEquals(81.34056, original.xVelocity, EPS);
        assertEquals(65.43028, original.yVelocity, EPS);
    }

    @Test
    public void swerveCopyDoesNotMutateOriginal() {
        SwerveConstants original = new SwerveConstants();
        SwerveConstants copy = original.copy();
        copy.xVelocity(12.0);
        assertEquals(80.0, original.xVelocity, EPS);
        assertEquals(12.0, copy.xVelocity, EPS);
    }
}
