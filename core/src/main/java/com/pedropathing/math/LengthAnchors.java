package com.pedropathing.math;

/**
 * Upstream Pedro defaults, authored in inches. {@link LengthUnit} converts these into
 * centimeters, meters, feet, or millimeters so a unit switch keeps follower behavior.
 *
 * <p>Set {@link LengthUnit#use(LengthUnit)} once. Constructors and {@code applyLengthUnit()}
 * look up {@link LengthUnit#active()} rather than taking a unit at every call site.
 *
 * @author The Allsparks - 36117
 */
public final class LengthAnchors {
    private LengthAnchors() {}

    public static final double DRIVE_PIDF_SWITCH = 20;
    public static final double TRANSLATIONAL_PIDF_SWITCH = 3;
    public static final double STUCK_VELOCITY = 1.0;
    public static final double FORWARD_ZERO_POWER_ACCELERATION = -41.278;
    public static final double LATERAL_ZERO_POWER_ACCELERATION = -59.7819;
    public static final double CENTRIPETAL_SCALING = 0.0005;
    public static final double KALMAN_MODEL_COVARIANCE = 6;
    public static final double KALMAN_DATA_COVARIANCE = 1;

    public static final double MECANUM_X_VELOCITY = 81.34056;
    public static final double MECANUM_Y_VELOCITY = 65.43028;
    public static final double SWERVE_VELOCITY = 80.0;

    public static final double PATH_VELOCITY_CONSTRAINT = 0.1;
    public static final double PATH_TRANSLATIONAL_CONSTRAINT = 0.1;

    /** Forward / lateral tuner pull distance. Two tiles. */
    public static final double TUNER_PULL = 48;
    /** Line / heading / drive tuner distance. */
    public static final double TUNER_LINE = 40;
    /** Centripetal tuner offset. */
    public static final double TUNER_CURVE = 20;
    /** Zero-power acceleration tuner trip velocity. */
    public static final double TUNER_VELOCITY = 30;
    /** Circle test radius. */
    public static final double TUNER_RADIUS = 10;

    public static double of(double inches) {
        return LengthUnit.ofInches(inches);
    }

    public static double of(double inches, LengthUnit unit) {
        return unit.fromInches(inches);
    }
}
