package com.pedropathing.ftc;
import com.pedropathing.drivetrain.Drivetrain;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.drivetrains.*;
import com.pedropathing.ftc.localization.constants.DriveEncoderConstants;
import com.pedropathing.ftc.localization.constants.OctoQuadConstants;
import com.pedropathing.ftc.localization.constants.OTOSConstants;
import com.pedropathing.ftc.localization.constants.PinpointConstants;
import com.pedropathing.ftc.localization.constants.ThreeWheelConstants;
import com.pedropathing.ftc.localization.constants.ThreeWheelIMUConstants;
import com.pedropathing.ftc.localization.constants.TwoWheelConstants;
import com.pedropathing.ftc.localization.localizers.DriveEncoderLocalizer;
import com.pedropathing.ftc.localization.localizers.OctoQuadLocalizer;
import com.pedropathing.ftc.localization.localizers.OTOSLocalizer;
import com.pedropathing.ftc.localization.localizers.PinpointLocalizer;
import com.pedropathing.ftc.localization.localizers.ThreeWheelIMULocalizer;
import com.pedropathing.ftc.localization.localizers.ThreeWheelLocalizer;
import com.pedropathing.ftc.localization.localizers.TwoWheelLocalizer;
import com.pedropathing.geometry.Pose;
import com.pedropathing.localization.Localizer;
import com.pedropathing.math.AngularUnit;
import com.pedropathing.math.LengthUnit;
import com.pedropathing.math.MassUnit;
import com.pedropathing.math.PedroUnits;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.robotcore.hardware.HardwareMap;

/** This is the FollowerBuilder.
 * It is used to create Followers with a specific drivetrain + localizer without having to use a full constructor.
 *
 * <p>{@link #setUnits} selects TeamCode-facing interface units for this builder and the resulting
 * follower. Pedro still calculates in inches, kilograms, and radians. Call {@code setUnits}
 * immediately after constructing the builder, at most once.
 *
 * @author Baron Henderson - 20077 The Indubitables
 * @author The Allsparks - 36117
 */
public class FollowerBuilder {
    private FollowerConstants constants;
    private boolean constantsCopied;
    private PathConstraints constraints;
    private boolean constraintsCopied;
    private final HardwareMap hardwareMap;
    private Localizer localizer;
    private Drivetrain drivetrain;
    private PedroUnits units = PedroUnits.DEFAULT;
    private boolean unitsConfigured;
    private boolean configuredInputsAccepted;
    private boolean hardwareConfigured;
    private Pose startingPose;
    private Double configuredXVelocity;
    private Double configuredYVelocity;

    public FollowerBuilder(FollowerConstants constants, HardwareMap hardwareMap) {
        this.constants = constants;
        this.hardwareMap = hardwareMap;
        this.constraints = PathConstraints.defaultConstraints;
    }

    /**
     * Select user-interface units for this builder. Call at most once, immediately after
     * constructing the builder and before any configured-unit setters.
     * Does not modify global state, {@link FollowerConstants}, drivetrain constants, or hardware
     * localizer constants.
     */
    public FollowerBuilder setUnits(PedroUnits units) {
        lockUnits(PedroUnits.requireNonNull(units));
        return this;
    }

    /**
     * Select user-interface units for this builder. Call at most once, immediately after
     * constructing the builder and before any configured-unit setters.
     */
    public FollowerBuilder setUnits(LengthUnit lengthUnit, MassUnit massUnit, AngularUnit angleUnit) {
        return setUnits(new PedroUnits(lengthUnit, massUnit, angleUnit));
    }

    /**
     * Select a length unit. Mass defaults to kilograms and angle defaults to radians.
     */
    public FollowerBuilder setUnits(LengthUnit lengthUnit) {
        return setUnits(lengthUnit, MassUnit.KILOGRAMS, AngularUnit.RADIANS);
    }

    /**
     * Select length and angle units. Mass defaults to kilograms.
     */
    public FollowerBuilder setUnits(LengthUnit lengthUnit, AngularUnit angleUnit) {
        return setUnits(lengthUnit, MassUnit.KILOGRAMS, angleUnit);
    }

    public PedroUnits getUnits() {
        return units;
    }

    /**
     * Robot mass in the configured mass unit. Converted immediately to kilograms on a copy of the
     * caller-supplied {@link FollowerConstants}.
     */
    public FollowerBuilder setMass(double mass) {
        writableConstants().mass(units.massToInternal(mass));
        return this;
    }

    /**
     * Starting pose in configured length and angle units. Converted immediately to inches and
     * radians.
     */
    public FollowerBuilder setStartingPose(double x, double y, double heading) {
        acceptConfiguredInput();
        this.startingPose = units.pose(x, y, heading);
        return this;
    }

    /**
     * Starting pose in configured length units with heading 0.
     */
    public FollowerBuilder setStartingPose(double x, double y) {
        return setStartingPose(x, y, 0);
    }

    /**
     * Canonical starting pose in inches and radians.
     */
    public FollowerBuilder setStartingPose(Pose pose) {
        this.startingPose = pose;
        return this;
    }

    /**
     * Forward zero-power acceleration in the configured length unit per second squared.
     * Converted immediately to inches per second squared on a constants copy.
     */
    public FollowerBuilder setForwardZeroPowerAcceleration(double acceleration) {
        writableConstants().forwardZeroPowerAcceleration(units.accelerationToInternal(acceleration));
        return this;
    }

    /**
     * Lateral zero-power acceleration in the configured length unit per second squared.
     * Converted immediately to inches per second squared on a constants copy.
     */
    public FollowerBuilder setLateralZeroPowerAcceleration(double acceleration) {
        writableConstants().lateralZeroPowerAcceleration(units.accelerationToInternal(acceleration));
        return this;
    }

    /**
     * Path completion velocity in the configured length unit per second.
     * Converted immediately to inches per second on a constraints copy.
     */
    public FollowerBuilder setPathCompletionVelocity(double velocity) {
        writableConstraints().setVelocityConstraint(units.velocityToInternal(velocity));
        return this;
    }

    /**
     * Path completion translational tolerance in the configured length unit.
     * Converted immediately to inches on a constraints copy.
     */
    public FollowerBuilder setPathCompletionTranslationalTolerance(double tolerance) {
        writableConstraints().setTranslationalConstraint(units.lengthToInternal(tolerance));
        return this;
    }

    /**
     * Path completion heading tolerance in the configured angle unit.
     * Converted immediately to radians on a constraints copy.
     */
    public FollowerBuilder setPathCompletionHeadingTolerance(double heading) {
        writableConstraints().setHeadingConstraint(units.angleToInternal(heading));
        return this;
    }

    /**
     * Drivetrain forward maximum velocity in the configured length unit per second.
     * Converted immediately to inches per second. Must be called before the drivetrain is
     * configured.
     */
    public FollowerBuilder setXVelocity(double xVelocity) {
        acceptConfiguredInput();
        if (hardwareConfigured) {
            throw new IllegalStateException("setXVelocity() must be called before configuring the drivetrain");
        }
        this.configuredXVelocity = units.velocityToInternal(xVelocity);
        return this;
    }

    /**
     * Drivetrain lateral maximum velocity in the configured length unit per second.
     * Converted immediately to inches per second. Must be called before the drivetrain is
     * configured.
     */
    public FollowerBuilder setYVelocity(double yVelocity) {
        acceptConfiguredInput();
        if (hardwareConfigured) {
            throw new IllegalStateException("setYVelocity() must be called before configuring the drivetrain");
        }
        this.configuredYVelocity = units.velocityToInternal(yVelocity);
        return this;
    }

    public FollowerBuilder setLocalizer(Localizer localizer) {
        this.localizer = localizer;
        hardwareConfigured = true;
        return this;
    }

    public FollowerBuilder driveEncoderLocalizer(DriveEncoderConstants lConstants) {
        return setLocalizer(new DriveEncoderLocalizer(hardwareMap, lConstants));
    }

    public FollowerBuilder octoQuadLocalizer(OctoQuadConstants lConstants, OctoQuadLocalizer.InitMode initMode) {
        return setLocalizer(new OctoQuadLocalizer(hardwareMap, lConstants, initMode));
    }

    public FollowerBuilder OTOSLocalizer(OTOSConstants lConstants) {
        return setLocalizer(new OTOSLocalizer(hardwareMap, lConstants));
    }

    public FollowerBuilder pinpointLocalizer(PinpointConstants lConstants) {
        return setLocalizer(new PinpointLocalizer(hardwareMap, lConstants));
    }

    public FollowerBuilder threeWheelIMULocalizer(ThreeWheelIMUConstants lConstants) {
        return setLocalizer(new ThreeWheelIMULocalizer(hardwareMap, lConstants));
    }

    public FollowerBuilder threeWheelLocalizer(ThreeWheelConstants lConstants) {
        return setLocalizer(new ThreeWheelLocalizer(hardwareMap, lConstants));
    }

    public FollowerBuilder twoWheelLocalizer(TwoWheelConstants lConstants) {
        return setLocalizer(new TwoWheelLocalizer(hardwareMap, lConstants));
    }

    public FollowerBuilder setDrivetrain(Drivetrain drivetrain) {
        this.drivetrain = drivetrain;
        hardwareConfigured = true;
        return this;
    }

    public FollowerBuilder mecanumDrivetrain(MecanumConstants mecanumConstants) {
        MecanumConstants resolved = applyVelocityOverrides(mecanumConstants);
        return setDrivetrain(new Mecanum(hardwareMap, resolved));
    }

    @Deprecated
    public FollowerBuilder mecanumExDrivetrain(MecanumConstants mecanumConstants) {
        MecanumConstants resolved = applyVelocityOverrides(mecanumConstants);
        return setDrivetrain(new MecanumEx(hardwareMap, resolved));
    }

    public FollowerBuilder swerveDrivetrain(SwerveConstants swerveConstants, SwervePod... pods) {
        SwerveConstants resolved = applyVelocityOverrides(swerveConstants);
        return setDrivetrain(new Swerve(hardwareMap, resolved, pods));
    }

    /**
     * Attach canonical path constraints. This does not mutate
     * {@link PathConstraints#defaultConstraints}.
     */
    public FollowerBuilder pathConstraints(PathConstraints pathConstraints) {
        this.constraints = pathConstraints;
        this.constraintsCopied = false;
        return this;
    }

    public Follower build() {
        Follower follower = new Follower(constants, localizer, drivetrain, constraints, units);
        if (startingPose != null) {
            follower.setStartingPose(startingPose);
        }
        return follower;
    }

    private void lockUnits(PedroUnits units) {
        if (unitsConfigured) {
            throw new IllegalStateException("setUnits(...) can only be called once. Call it immediately after constructing FollowerBuilder.");
        }
        if (configuredInputsAccepted) {
            throw new IllegalStateException("setUnits(...) must be called before any configured-unit setters.");
        }
        this.units = units;
        this.unitsConfigured = true;
    }

    private void acceptConfiguredInput() {
        configuredInputsAccepted = true;
    }

    private FollowerConstants writableConstants() {
        acceptConfiguredInput();
        if (!constantsCopied) {
            constants = constants.copy();
            constantsCopied = true;
        }
        return constants;
    }

    private PathConstraints writableConstraints() {
        acceptConfiguredInput();
        if (!constraintsCopied) {
            constraints = constraints.copy();
            constraintsCopied = true;
        }
        return constraints;
    }

    private MecanumConstants applyVelocityOverrides(MecanumConstants mecanumConstants) {
        if (configuredXVelocity == null && configuredYVelocity == null) {
            return mecanumConstants;
        }
        MecanumConstants copy = mecanumConstants.copy();
        if (configuredXVelocity != null) {
            copy.xVelocity(configuredXVelocity);
        }
        if (configuredYVelocity != null) {
            copy.yVelocity(configuredYVelocity);
        }
        return copy;
    }

    private SwerveConstants applyVelocityOverrides(SwerveConstants swerveConstants) {
        if (configuredXVelocity == null && configuredYVelocity == null) {
            return swerveConstants;
        }
        SwerveConstants copy = swerveConstants.copy();
        if (configuredXVelocity != null) {
            copy.xVelocity(configuredXVelocity);
        }
        if (configuredYVelocity != null) {
            copy.yVelocity(configuredYVelocity);
        }
        return copy;
    }
}
