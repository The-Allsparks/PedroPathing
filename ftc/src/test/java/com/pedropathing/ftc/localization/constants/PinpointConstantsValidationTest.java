package com.pedropathing.ftc.localization.constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.math.AngularUnit;
import com.pedropathing.math.LengthUnit;
import com.pedropathing.math.MassUnit;
import com.pedropathing.math.PedroUnits;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.junit.Test;

public class PinpointConstantsValidationTest {
    private static final double EPS = 1e-9;

    @Test
    public void defaultInchOffsetsValidate() {
        new PinpointConstants().validate();
    }

    @Test
    public void changingUnitWithoutOffsetsThrows() {
        try {
            new PinpointConstants().distanceUnit(DistanceUnit.CM).validate();
            fail("expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("CM"));
        }
    }

    @Test
    public void metersWithoutOffsetsThrows() {
        try {
            new PinpointConstants().distanceUnit(DistanceUnit.METER).validate();
            fail("expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("METER"));
        }
    }

    @Test
    public void convertExistingMeasurementsKeepsDefaultGeometry() {
        PinpointConstants constants = new PinpointConstants().distanceUnit(DistanceUnit.CM, true);
        constants.validate();
        assertEquals(DistanceUnit.CM, constants.distanceUnit);
        assertEquals(2.54, constants.forwardPodY, EPS);
        assertEquals(-6.35, constants.strafePodX, EPS);
    }

    @Test
    public void explicitOffsetsInNewUnitValidate() {
        PinpointConstants constants = new PinpointConstants()
                .distanceUnit(DistanceUnit.CM)
                .forwardPodY(5.0)
                .strafePodX(-8.0);
        constants.validate();
        assertEquals(5.0, constants.forwardPodY, EPS);
        assertEquals(-8.0, constants.strafePodX, EPS);
    }

    @Test
    public void partialOffsetUpdateThrows() {
        try {
            new PinpointConstants().distanceUnit(DistanceUnit.MM).forwardPodY(50.8).validate();
            fail("expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("pod offsets"));
        }
    }

    @Test
    public void alignDistanceUnitMatchesTeamCodeCentimeters() {
        PedroUnits units = new PedroUnits(LengthUnit.CENTIMETERS, MassUnit.KILOGRAMS, AngularUnit.RADIANS);
        PinpointConstants constants = new PinpointConstants().alignDistanceUnit(units);
        constants.validate();
        assertEquals(DistanceUnit.CM, constants.distanceUnit);
        assertEquals(2.54, constants.forwardPodY, EPS);
        assertEquals(-6.35, constants.strafePodX, EPS);
    }

    @Test
    public void alignFeetKeepsPinpointInches() {
        PinpointConstants constants = new PinpointConstants().alignDistanceUnit(LengthUnit.FEET);
        constants.validate();
        assertEquals(DistanceUnit.INCH, constants.distanceUnit);
        assertEquals(1.0, constants.forwardPodY, EPS);
        assertEquals(-2.5, constants.strafePodX, EPS);
    }

    @Test
    public void customEncoderResolutionConvertsWithUnit() {
        PinpointConstants constants = new PinpointConstants()
                .customEncoderResolution(25.4)
                .distanceUnit(DistanceUnit.MM, true);
        constants.validate();
        assertEquals(1.0, constants.customEncoderResolution.getAsDouble(), EPS);
        assertEquals(25.4, constants.forwardPodY, EPS);
    }

    @Test
    public void oversizedMeterOffsetThrows() {
        try {
            new PinpointConstants()
                    .distanceUnit(DistanceUnit.METER)
                    .forwardPodY(1.0)
                    .strafePodX(-0.05)
                    .validate();
            fail("expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("forwardPodY"));
        }
    }

    @Test
    public void builderRejectsUnconvertedUnitChangeBeforeHardware() {
        try {
            new FollowerBuilder(new FollowerConstants(), null)
                    .pinpointLocalizer(new PinpointConstants().distanceUnit(DistanceUnit.CM));
            fail("expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("distanceUnit"));
        }
    }
}
