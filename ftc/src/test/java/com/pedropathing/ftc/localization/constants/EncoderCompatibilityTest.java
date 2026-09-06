package com.pedropathing.ftc.localization.constants;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EncoderCompatibilityTest {
    private static final double EPS = 1e-12;

    @Test
    public void deprecatedDriveEncoderSettersWriteNewFields() {
        DriveEncoderConstants constants = new DriveEncoderConstants()
                .forwardTicksToInches(0.002)
                .strafeTicksToInches(0.003)
                .turnTicksToInches(0.004);
        assertEquals(0.002, constants.forwardTicksToDistance, EPS);
        assertEquals(0.003, constants.strafeTicksToDistance, EPS);
        assertEquals(0.004, constants.turnTicksToRadians, EPS);
    }

    @Test
    public void deprecatedThreeWheelSettersWriteNewFields() {
        ThreeWheelConstants constants = new ThreeWheelConstants()
                .forwardTicksToInches(0.01)
                .strafeTicksToInches(0.02)
                .turnTicksToInches(0.03);
        assertEquals(0.01, constants.forwardTicksToDistance, EPS);
        assertEquals(0.02, constants.strafeTicksToDistance, EPS);
        assertEquals(0.03, constants.turnTicksToRadians, EPS);
    }

    @Test
    public void deprecatedTwoWheelSettersWriteNewFields() {
        TwoWheelConstants constants = new TwoWheelConstants()
                .forwardTicksToInches(0.01)
                .strafeTicksToInches(0.02);
        assertEquals(0.01, constants.forwardTicksToDistance, EPS);
        assertEquals(0.02, constants.strafeTicksToDistance, EPS);
    }

    @Test
    public void centimeterPerTickContractIsExplicit() {
        DriveEncoderConstants constants = new DriveEncoderConstants()
                .forwardTicksToDistance(0.05)
                .robotWidth(30)
                .robotLength(35);
        assertEquals(0.05, constants.forwardTicksToDistance, EPS);
        assertEquals(30.0, constants.robot_Width, EPS);
        assertEquals(35.0, constants.robot_Length, EPS);
    }
}
