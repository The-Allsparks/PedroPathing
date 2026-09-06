package com.pedropathing.ftc.localization.constants;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EncoderCompatibilityTest {
    private static final double EPS = 1e-12;

    @Test
    public void upstreamFieldNamesRemainInchesPerTick() {
        DriveEncoderConstants constants = new DriveEncoderConstants()
                .forwardTicksToInches(0.002)
                .strafeTicksToInches(0.003)
                .turnTicksToInches(0.004);
        assertEquals(0.002, constants.forwardTicksToInches, EPS);
        assertEquals(0.003, constants.strafeTicksToInches, EPS);
        assertEquals(0.004, constants.turnTicksToInches, EPS);
    }

    @Test
    public void clearerAliasesWriteTheSameFields() {
        DriveEncoderConstants constants = new DriveEncoderConstants()
                .forwardInchesPerTick(0.05)
                .strafeInchesPerTick(0.06)
                .turnRadiansPerTick(0.07);
        assertEquals(0.05, constants.forwardTicksToInches, EPS);
        assertEquals(0.06, constants.strafeTicksToInches, EPS);
        assertEquals(0.07, constants.turnTicksToInches, EPS);
    }

    @Test
    public void deprecatedDistanceAliasesWriteInchFields() {
        DriveEncoderConstants drive = new DriveEncoderConstants().forwardTicksToDistance(0.05);
        assertEquals(0.05, drive.forwardTicksToInches, EPS);
        ThreeWheelConstants three = new ThreeWheelConstants().strafeTicksToDistance(0.02);
        assertEquals(0.02, three.strafeTicksToInches, EPS);
        TwoWheelConstants two = new TwoWheelConstants().forwardTicksToDistance(0.01);
        assertEquals(0.01, two.forwardTicksToInches, EPS);
    }
}
