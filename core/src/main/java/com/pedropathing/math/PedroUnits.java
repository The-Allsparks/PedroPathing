package com.pedropathing.math;

import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.BezierPoint;
import com.pedropathing.geometry.CoordinateSystem;
import com.pedropathing.geometry.Curve;
import com.pedropathing.geometry.PedroCoordinates;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathConstraints;

import java.util.ArrayList;

/**
 * Immutable user-interface unit configuration for Pedro Pathing.
 *
 * <p>Pedro always calculates in inches, kilograms, and radians. A {@code PedroUnits} instance
 * converts TeamCode-facing values into those canonical units at follower and path-builder
 * boundaries. After {@code setUnits}, TeamCode pose, path, heading, and telemetry interaction
 * uses the selected units with no mixing. It has no process-wide mutable state and is safe to
 * reuse among followers.
 *
 * @author The Allsparks - 36117
 */
public final class PedroUnits {
    public static final PedroUnits DEFAULT = new PedroUnits(
            LengthUnit.INCHES,
            MassUnit.KILOGRAMS,
            AngularUnit.RADIANS);

    private final LengthUnit lengthUnit;
    private final MassUnit massUnit;
    private final AngularUnit angleUnit;

    public PedroUnits(LengthUnit lengthUnit, MassUnit massUnit, AngularUnit angleUnit) {
        this.lengthUnit = LengthUnit.requireNonNull(lengthUnit);
        this.massUnit = MassUnit.requireNonNull(massUnit);
        this.angleUnit = AngularUnit.requireNonNull(angleUnit);
    }

    public static PedroUnits requireNonNull(PedroUnits units) {
        if (units == null) {
            throw new IllegalArgumentException("Pedro units must not be null");
        }
        return units;
    }

    public LengthUnit lengthUnit() {
        return lengthUnit;
    }

    public MassUnit massUnit() {
        return massUnit;
    }

    public AngularUnit angleUnit() {
        return angleUnit;
    }

    public double lengthToInternal(double value) {
        return lengthUnit.toInches(value);
    }

    public double lengthFromInternal(double inches) {
        return lengthUnit.fromInches(inches);
    }

    public double velocityToInternal(double value) {
        return lengthUnit.toInches(value);
    }

    public double velocityFromInternal(double inchesPerSecond) {
        return lengthUnit.fromInches(inchesPerSecond);
    }

    public double accelerationToInternal(double value) {
        return lengthUnit.toInches(value);
    }

    public double accelerationFromInternal(double inchesPerSecondSquared) {
        return lengthUnit.fromInches(inchesPerSecondSquared);
    }

    public double massToInternal(double value) {
        return massUnit.toKilograms(value);
    }

    public double massFromInternal(double kilograms) {
        return massUnit.fromKilograms(kilograms);
    }

    public double angleToInternal(double value) {
        return angleUnit.toRadians(value);
    }

    public double angleFromInternal(double radians) {
        return angleUnit.fromRadians(radians);
    }

    public double tile() {
        return lengthUnit.tile();
    }

    public double fieldSize() {
        return lengthUnit.fieldSize();
    }

    public double fieldCenter() {
        return lengthUnit.fieldCenter();
    }

    public double mirrorFieldLength() {
        return lengthUnit.mirrorFieldLength();
    }

    /**
     * Create a TeamCode {@link Pose} whose stored numbers are already in this configuration.
     * Pass it to follower and path-builder APIs; they convert to inches and radians internally.
     */
    public Pose pose(double x, double y, double heading) {
        return pose(x, y, heading, PedroCoordinates.INSTANCE);
    }

    /**
     * Create a TeamCode {@link Pose} whose stored numbers are already in this configuration.
     */
    public Pose pose(double x, double y) {
        return pose(x, y, 0);
    }

    /**
     * Create a TeamCode {@link Pose} whose stored numbers are already in this configuration.
     */
    public Pose pose(double x, double y, double heading, CoordinateSystem coordinateSystem) {
        return new Pose(x, y, heading, coordinateSystem);
    }

    /**
     * Convert a TeamCode pose in this configuration into canonical inches and radians.
     */
    public Pose toInternalPose(Pose userPose) {
        requirePose(userPose);
        return new Pose(
                lengthToInternal(userPose.getX()),
                lengthToInternal(userPose.getY()),
                angleToInternal(userPose.getHeading()),
                userPose.getCoordinateSystem());
    }

    /**
     * Convert a canonical Pedro pose (inches and radians) into this configuration.
     */
    public Pose toUserPose(Pose internalPose) {
        requirePose(internalPose);
        return new Pose(
                lengthFromInternal(internalPose.getX()),
                lengthFromInternal(internalPose.getY()),
                angleFromInternal(internalPose.getHeading()),
                internalPose.getCoordinateSystem());
    }

    /**
     * Convert a canonical length vector into this configuration. Direction stays in radians,
     * matching {@link Vector}.
     */
    public Vector toUserVector(Vector internal) {
        if (internal == null) {
            return null;
        }
        Vector user = new Vector();
        user.setOrthogonalComponents(
                lengthFromInternal(internal.getXComponent()),
                lengthFromInternal(internal.getYComponent()));
        return user;
    }

    /**
     * Convert a curve whose control points are in this configuration into canonical inches
     * and radians. Identity configurations return the original curve.
     */
    public Curve toInternalCurve(Curve curve) {
        if (curve == null || equals(DEFAULT)) {
            return curve;
        }
        ArrayList<Pose> points = curve.getControlPoints();
        if (points == null || points.isEmpty()) {
            return curve;
        }
        ArrayList<Pose> converted = new ArrayList<>(points.size());
        for (Pose point : points) {
            if (point == null) {
                return curve;
            }
            converted.add(toInternalPose(point));
        }
        if (converted.size() == 1) {
            return new BezierPoint(converted.get(0));
        }
        if (converted.size() == 2) {
            return new BezierLine(converted.get(0), converted.get(1));
        }
        return new BezierCurve(converted, curve.getPathConstraints());
    }

    /**
     * Convert a path whose curve is in this configuration into canonical inches and radians.
     * Heading interpolators and constraints are copied as-is (they are already canonical once
     * PathBuilder numeric APIs have converted them).
     */
    public Path toInternalPath(Path path) {
        if (path == null || equals(DEFAULT)) {
            return path;
        }
        Path converted = new Path(toInternalCurve(path.getCurve()), path.getConstraints());
        converted.setHeadingInterpolation(path.getHeadingInterpolator());
        return converted;
    }

    public static boolean differs(double value, double defaultValue) {
        return Double.compare(value, defaultValue) != 0;
    }

    public double x(Pose pose) {
        return requirePose(pose).getX();
    }

    public double y(Pose pose) {
        return requirePose(pose).getY();
    }

    public double heading(Pose pose) {
        return requirePose(pose).getHeading();
    }

    /**
     * Present a canonical Pedro pose in this configuration's interface units, including unit
     * symbols. Prefer {@link #toUserPose(Pose)} when you need a {@link Pose}.
     */
    public ConfiguredPose fromInternalPose(Pose pose) {
        requirePose(pose);
        return new ConfiguredPose(
                lengthFromInternal(pose.getX()),
                lengthFromInternal(pose.getY()),
                angleFromInternal(pose.getHeading()),
                this,
                pose.getCoordinateSystem());
    }

    public String formatPose(Pose pose) {
        return fromInternalPose(pose).toString();
    }

    public ConfiguredPathConstraintsBuilder pathConstraints() {
        return new ConfiguredPathConstraintsBuilder(this);
    }

    private static Pose requirePose(Pose pose) {
        if (pose == null) {
            throw new IllegalArgumentException("pose must not be null");
        }
        return pose;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PedroUnits)) {
            return false;
        }
        PedroUnits units = (PedroUnits) other;
        return lengthUnit == units.lengthUnit
                && massUnit == units.massUnit
                && angleUnit == units.angleUnit;
    }

    @Override
    public int hashCode() {
        int result = lengthUnit.hashCode();
        result = 31 * result + massUnit.hashCode();
        result = 31 * result + angleUnit.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PedroUnits("
                + lengthUnit.pluralName()
                + ", "
                + massUnit.pluralName()
                + ", "
                + angleUnit.pluralName()
                + ")";
    }

    /**
     * Builds canonical {@link PathConstraints} from values expressed in this configuration.
     * Does not mutate {@link PathConstraints#defaultConstraints}.
     */
    public static final class ConfiguredPathConstraintsBuilder {
        private final PedroUnits units;
        private double tValueConstraint;
        private Double velocityConstraint;
        private Double translationalConstraint;
        private Double headingConstraint;
        private double timeoutConstraint;
        private double brakingStrength;
        private int searchLimit;
        private double brakingStart;

        private ConfiguredPathConstraintsBuilder(PedroUnits units) {
            this.units = units;
            PathConstraints defaults = PathConstraints.defaultConstraints;
            this.tValueConstraint = defaults.getTValueConstraint();
            this.timeoutConstraint = defaults.getTimeoutConstraint();
            this.brakingStrength = defaults.getBrakingStrength();
            this.searchLimit = defaults.getBEZIER_CURVE_SEARCH_LIMIT();
            this.brakingStart = defaults.getBrakingStart();
        }

        public ConfiguredPathConstraintsBuilder tValueConstraint(double tValueConstraint) {
            this.tValueConstraint = tValueConstraint;
            return this;
        }

        /** Path completion velocity in the configured length unit per second. */
        public ConfiguredPathConstraintsBuilder velocityConstraint(double velocityConstraint) {
            this.velocityConstraint = velocityConstraint;
            return this;
        }

        /** Path completion translational tolerance in the configured length unit. */
        public ConfiguredPathConstraintsBuilder translationalConstraint(double translationalConstraint) {
            this.translationalConstraint = translationalConstraint;
            return this;
        }

        /** Path completion heading tolerance in the configured angle unit. */
        public ConfiguredPathConstraintsBuilder headingConstraint(double headingConstraint) {
            this.headingConstraint = headingConstraint;
            return this;
        }

        public ConfiguredPathConstraintsBuilder timeoutConstraint(double timeoutConstraint) {
            this.timeoutConstraint = timeoutConstraint;
            return this;
        }

        public ConfiguredPathConstraintsBuilder brakingStrength(double brakingStrength) {
            this.brakingStrength = brakingStrength;
            return this;
        }

        public ConfiguredPathConstraintsBuilder searchLimit(int searchLimit) {
            this.searchLimit = searchLimit;
            return this;
        }

        public ConfiguredPathConstraintsBuilder brakingStart(double brakingStart) {
            this.brakingStart = brakingStart;
            return this;
        }

        /**
         * Produce a canonical {@link PathConstraints} (inches per second, inches, radians).
         * The shared default constraints object is not mutated.
         */
        public PathConstraints build() {
            PathConstraints constraints = PathConstraints.defaultConstraints.copy();
            constraints.setTValueConstraint(tValueConstraint);
            constraints.setTimeoutConstraint(timeoutConstraint);
            constraints.setBrakingStrength(brakingStrength);
            constraints.setBEZIER_CURVE_SEARCH_LIMIT(searchLimit);
            constraints.setBrakingStart(brakingStart);
            if (velocityConstraint != null) {
                constraints.setVelocityConstraint(units.velocityToInternal(velocityConstraint));
            }
            if (translationalConstraint != null) {
                constraints.setTranslationalConstraint(units.lengthToInternal(translationalConstraint));
            }
            if (headingConstraint != null) {
                constraints.setHeadingConstraint(units.angleToInternal(headingConstraint));
            }
            return constraints;
        }
    }
}
