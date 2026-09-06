package com.pedropathing.ftc.localization.constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.junit.Test;

public class HardwareConstantsIndependenceTest {
    @Test
    public void pinpointKeepsCallerHardwareUnit() {
        PinpointConstants constants = new PinpointConstants().distanceUnit(DistanceUnit.INCH).forwardPodY(1.5);
        assertEquals(DistanceUnit.INCH, constants.distanceUnit);
        assertEquals(1.5, constants.forwardPodY, 0.0);
        assertSame(DistanceUnit.INCH, constants.distanceUnit);
    }

    @Test
    public void otosKeepsCallerLinearUnitAndOffset() {
        OTOSConstants constants = new OTOSConstants().linearUnit(DistanceUnit.INCH);
        assertEquals(DistanceUnit.INCH, constants.linearUnit);
        assertEquals(0.0, constants.offset.x, 0.0);
        assertEquals(0.0, constants.offset.y, 0.0);
    }
}
