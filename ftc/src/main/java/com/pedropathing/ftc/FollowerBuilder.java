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
import com.pedropathing.localization.Localizer;
import com.pedropathing.math.LengthUnit;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.robotcore.hardware.HardwareMap;

/** This is the FollowerBuilder.
 * It is used to create Followers with a specific drivetrain + localizer without having to use a full constructor
 *
 * @author Baron Henderson - 20077 The Indubitables
 */
public class FollowerBuilder {
    private final FollowerConstants constants;
    private PathConstraints constraints;
    private final HardwareMap hardwareMap;
    private Localizer localizer;
    private Drivetrain drivetrain;
    private LengthUnit followerUnit;
    private boolean hardwareConfigured;

    public FollowerBuilder(FollowerConstants constants, HardwareMap hardwareMap) {
        this.constants = constants;
        this.hardwareMap = hardwareMap;
        this.followerUnit = constants.getLengthUnit();
        this.constraints = PathConstraints.defaultsFor(followerUnit);
    }

    /**
     * Select the follower length unit for this builder. Must be called before configuring the
     * localizer or drivetrain. Does not mutate the supplied {@link FollowerConstants}.
     */
    public FollowerBuilder lengthUnit(LengthUnit followerUnit) {
        if (hardwareConfigured) {
            throw new IllegalStateException("lengthUnit() must be called before configuring the localizer or drivetrain");
        }
        this.followerUnit = LengthUnit.requireNonNull(followerUnit);
        this.constraints = PathConstraints.defaultsFor(this.followerUnit);
        return this;
    }

    public LengthUnit getLengthUnit() {
        return followerUnit;
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
        return setLocalizer(new OctoQuadLocalizer(hardwareMap, lConstants, initMode, followerUnit));
    }

    public FollowerBuilder OTOSLocalizer(OTOSConstants lConstants) {
        return setLocalizer(new OTOSLocalizer(hardwareMap, lConstants, followerUnit));
    }

    public FollowerBuilder pinpointLocalizer(PinpointConstants lConstants) {
        return setLocalizer(new PinpointLocalizer(hardwareMap, lConstants, followerUnit));
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
        return setDrivetrain(new Mecanum(hardwareMap, mecanumConstants.inUnit(followerUnit)));
    }

    @Deprecated
    public FollowerBuilder mecanumExDrivetrain(MecanumConstants mecanumConstants) {
        return setDrivetrain(new MecanumEx(hardwareMap, mecanumConstants.inUnit(followerUnit)));
    }

    public FollowerBuilder swerveDrivetrain(SwerveConstants swerveConstants, SwervePod... pods) {
        return setDrivetrain(new Swerve(hardwareMap, swerveConstants.inUnit(followerUnit), pods));
    }

    public FollowerBuilder pathConstraints(PathConstraints pathConstraints) {
        this.constraints = pathConstraints;
        return this;
    }

    public Follower build() {
        FollowerConstants followerConstants = constants.inUnit(followerUnit);
        PathConstraints resolvedConstraints = constraints.inUnit(followerUnit);
        return new Follower(followerConstants, localizer, drivetrain, resolvedConstraints);
    }
}
