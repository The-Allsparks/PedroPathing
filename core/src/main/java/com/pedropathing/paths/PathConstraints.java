package com.pedropathing.paths;

import com.pedropathing.math.LengthAnchors;
import com.pedropathing.math.LengthUnit;

public final class PathConstraints {
    /**
     * When the robot is at the end of its current Path or PathChain and the velocity goes below
     * this value, then end the Path. This is in this object's {@link #getLengthUnit()} per second.
     * This can be custom set for each Path.
     * Default Value: 0.1 inches/second
     */
    private double velocityConstraint;

    /**
     * When the robot is at the end of its current Path or PathChain and the translational error
     * goes below this value, then end the Path. This is in this object's {@link #getLengthUnit()}.
     * This can be custom set for each Path.
     * Default Value: 0.1 inches
     */
    private double translationalConstraint;

    /**
     * When the robot is at the end of its current Path or PathChain and the heading error goes
     * below this value, then end the Path. This is in radians.
     * This can be custom set for each Path.
     * Default Value: 0.007
     */
    private double headingConstraint;

    /**
     * When the t-value of the closest point to the robot on the Path is greater than this value,
     * then the Path is considered at its end.
     * This can be custom set for each Path.
     * Default Value: 0.995
     */
    private double tValueConstraint;

    /**
     * When the Path is considered at its end parametrically, then the Follower has this many
     * milliseconds to further correct by default.
     * This can be custom set for each Path.
     * Default Value: 100
     */
    private double timeoutConstraint;

    /**
     * A multiplier for the zero power acceleration to change the speed the robot decelerates at
     * the end of paths.
     * Increasing this will cause the robot to try to decelerate faster, at the risk of overshoots
     * or localization slippage.
     * Decreasing this will cause the deceleration at the end of the Path to be slower, making the
     * robot slower but reducing risk of end-of-path overshoots or localization slippage.
     * This can be set individually for each Path, but this is the default.
     * Default Value: 1
     */
    private double brakingStrength;

    /**
     * Multiplier for when the path should start its deceleration
     */
    private double brakingStart;

    /**
     * The number of steps in searching for the closest point
     */
    private int BEZIER_CURVE_SEARCH_LIMIT;

    /**
     * Unit of {@link #velocityConstraint} and {@link #translationalConstraint}.
     * Setters write values already expressed in this unit.
     */
    private LengthUnit lengthUnit;

    /**
     * Construct constraints whose velocity and translational values are already in {@code unit}.
     * These values are stored as-is and are not converted again.
     */
    public PathConstraints(
            LengthUnit unit,
            double tValueConstraint,
            double velocityConstraint,
            double translationalConstraint,
            double headingConstraint,
            double timeoutConstraint,
            double brakingStrength,
            int BEZIER_CURVE_SEARCH_LIMIT,
            double brakingStart) {
        this.lengthUnit = LengthUnit.requireNonNull(unit);
        this.tValueConstraint = tValueConstraint;
        this.velocityConstraint = velocityConstraint;
        this.translationalConstraint = translationalConstraint;
        this.headingConstraint = headingConstraint;
        this.timeoutConstraint = timeoutConstraint;
        this.brakingStrength = brakingStrength;
        this.brakingStart = brakingStart;
        this.BEZIER_CURVE_SEARCH_LIMIT = BEZIER_CURVE_SEARCH_LIMIT;
    }

    /**
     * Construct constraints whose velocity and translational values are in inches.
     * Prefer {@link #inUnit} or {@link #defaultsFor(LengthUnit)} when using another follower unit.
     */
    public PathConstraints(double tValueConstraint, double velocityConstraint, double translationalConstraint, double headingConstraint, double timeoutConstraint, double brakingStrength, int BEZIER_CURVE_SEARCH_LIMIT, double brakingStart) {
        this(LengthUnit.INCHES, tValueConstraint, velocityConstraint, translationalConstraint, headingConstraint, timeoutConstraint, brakingStrength, BEZIER_CURVE_SEARCH_LIMIT, brakingStart);
    }

    /**
     * Construct constraints with caller t-value, timeout, and braking values. Velocity and
     * translational defaults are the inch-authored Pedro anchors and are labeled inches.
     * Convert with {@link #inUnit(LengthUnit)} when attaching to a non-inch follower.
     */
    public PathConstraints(double tValueConstraint, double timeoutConstraint, double brakingStrength, double brakingStart) {
        this(LengthUnit.INCHES, tValueConstraint, LengthAnchors.PATH_VELOCITY_CONSTRAINT, LengthAnchors.PATH_TRANSLATIONAL_CONSTRAINT, 0.007, timeoutConstraint, brakingStrength, 10, brakingStart);
    }

    /**
     * Construct constraints with caller t-value and timeout. Velocity and translational defaults
     * are the inch-authored Pedro anchors and are labeled inches.
     */
    public PathConstraints(double tValueConstraint, double timeoutConstraint) {
        this(tValueConstraint, timeoutConstraint, 1, 1);
    }

    /**
     * Inch-authored Pedro default constraints. Unit-aware code must not mutate this object.
     * Use {@link #defaultsFor(LengthUnit)} to get a correctly scaled copy.
     */
    public static PathConstraints defaultConstraints = new PathConstraints(
            LengthUnit.INCHES,
            0.995,
            LengthAnchors.PATH_VELOCITY_CONSTRAINT,
            LengthAnchors.PATH_TRANSLATIONAL_CONSTRAINT,
            0.007,
            100,
            1,
            10,
            1);

    /**
     * Values already expressed in {@code unit}. They are stored as-is.
     */
    public static PathConstraints inUnit(
            LengthUnit unit,
            double tValueConstraint,
            double velocityConstraint,
            double translationalConstraint,
            double headingConstraint,
            double timeoutConstraint,
            double brakingStrength,
            int searchLimit,
            double brakingStart) {
        return new PathConstraints(
                unit,
                tValueConstraint,
                velocityConstraint,
                translationalConstraint,
                headingConstraint,
                timeoutConstraint,
                brakingStrength,
                searchLimit,
                brakingStart);
    }

    /**
     * Values authored in inches.
     */
    public static PathConstraints fromInches(
            double tValueConstraint,
            double velocityConstraint,
            double translationalConstraint,
            double headingConstraint,
            double timeoutConstraint,
            double brakingStrength,
            int searchLimit,
            double brakingStart) {
        return inUnit(
                LengthUnit.INCHES,
                tValueConstraint,
                velocityConstraint,
                translationalConstraint,
                headingConstraint,
                timeoutConstraint,
                brakingStrength,
                searchLimit,
                brakingStart);
    }

    /**
     * Inch-authored Pedro defaults converted exactly once into {@code unit}.
     */
    public static PathConstraints defaultsFor(LengthUnit unit) {
        return defaultConstraints.inUnit(LengthUnit.requireNonNull(unit));
    }

    /**
     * Return a copy whose velocity and translational constraints are expressed in {@code unit}.
     * Conversion uses this object's stored source unit. Applying twice with the same unit is a
     * no-op besides copying. This object is not mutated.
     */
    public PathConstraints inUnit(LengthUnit unit) {
        LengthUnit.requireNonNull(unit);
        PathConstraints copy = copy();
        if (unit != copy.lengthUnit) {
            copy.velocityConstraint = LengthUnit.rescale(copy.velocityConstraint, copy.lengthUnit, unit);
            copy.translationalConstraint = LengthUnit.rescale(copy.translationalConstraint, copy.lengthUnit, unit);
            copy.lengthUnit = unit;
        }
        return copy;
    }

    public LengthUnit getLengthUnit() {
        return lengthUnit;
    }

    public double getVelocityConstraint() {
        return velocityConstraint;
    }

    public double getTranslationalConstraint() {
        return translationalConstraint;
    }

    public double getHeadingConstraint() {
        return headingConstraint;
    }

    public double getTValueConstraint() {
        return tValueConstraint;
    }

    public double getTimeoutConstraint() {
        return timeoutConstraint;
    }

    public double getBrakingStrength() {
        return brakingStrength;
    }

    public double getBrakingStart() {
        return brakingStart;
    }

    public int getBEZIER_CURVE_SEARCH_LIMIT() {
        return BEZIER_CURVE_SEARCH_LIMIT;
    }

    /**
     * Replace the shared inch-authored default. Prefer {@link #defaultsFor(LengthUnit)} in
     * unit-aware code so constructing one follower cannot change future paths.
     */
    public static void setDefaultConstraints(PathConstraints defaultConstraints) {
        PathConstraints.defaultConstraints = defaultConstraints;
    }

    public void setBEZIER_CURVE_SEARCH_LIMIT(int BEZIER_CURVE_SEARCH_LIMIT) {
        this.BEZIER_CURVE_SEARCH_LIMIT = BEZIER_CURVE_SEARCH_LIMIT;
    }

    public void setBrakingStart(double brakingStart) {
        this.brakingStart = brakingStart;
    }

    public void setBrakingStrength(double brakingStrength) {
        this.brakingStrength = brakingStrength;
    }

    public void setHeadingConstraint(double headingConstraint) {
        this.headingConstraint = headingConstraint;
    }

    public void setTimeoutConstraint(double timeoutConstraint) {
        this.timeoutConstraint = timeoutConstraint;
    }

    /**
     * Set the translational end constraint in this object's {@link #getLengthUnit()}.
     */
    public void setTranslationalConstraint(double translationalConstraint) {
        this.translationalConstraint = translationalConstraint;
    }

    public void setTValueConstraint(double tValueConstraint) {
        this.tValueConstraint = tValueConstraint;
    }

    /**
     * Set the velocity end constraint in this object's {@link #getLengthUnit()} per second.
     */
    public void setVelocityConstraint(double velocityConstraint) {
        this.velocityConstraint = velocityConstraint;
    }

    public PathConstraints copy() {
        return new PathConstraints(
                lengthUnit,
                tValueConstraint,
                velocityConstraint,
                translationalConstraint,
                headingConstraint,
                timeoutConstraint,
                brakingStrength,
                BEZIER_CURVE_SEARCH_LIMIT,
                brakingStart);
    }
}
