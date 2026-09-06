package com.pedropathing.follower;

import com.pedropathing.ErrorCalculator;
import com.pedropathing.VectorCalculator;
import com.pedropathing.control.FilteredPIDFCoefficients;
import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.drivetrain.Drivetrain;
import com.pedropathing.paths.PathConstraints;
import com.pedropathing.paths.PathPoint;
import com.pedropathing.util.PoseHistory;

import com.pedropathing.localization.Localizer;
import com.pedropathing.geometry.Pose;
import com.pedropathing.localization.PoseTracker;
import com.pedropathing.math.ConfiguredPose;
import com.pedropathing.math.PedroUnits;
import com.pedropathing.geometry.BezierPoint;
import com.pedropathing.math.MathFunctions;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathBuilder;
import com.pedropathing.paths.callbacks.PathCallback;
import com.pedropathing.paths.PathChain;
import com.pedropathing.math.Vector;
import com.pedropathing.util.Timer;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * This is the Follower class. It handles the actual following of the paths and all the on-the-fly
 * calculations that are relevant for movement.
 *
 * @author Baron Henderson - 20077 The Indubitables
 * @author Anyi Lin - 10158 Scott's Bots
 * @author Aaron Yang - 10158 Scott's Bots
 * @author Harrison Womack - 10158 Scott's Bots
 * @version 1.1.0, 5/1/2025
 */
public class Follower {
    public FollowerConstants constants;
    public PathConstraints pathConstraints;
    private final PedroUnits units;
    public PoseTracker poseTracker;
    public ErrorCalculator errorCalculator;
    public VectorCalculator vectorCalculator;
    public Drivetrain drivetrain;
    private final PoseHistory poseHistory;
    private Pose currentPose = new Pose();
    private PathPoint closestPose = new PathPoint();
    private PathPoint previousClosestPose = new PathPoint();
    private Path currentPath = null;
    private PathChain currentPathChain = null;

    private int BEZIER_CURVE_SEARCH_LIMIT;
    private int chainIndex;
    private boolean followingPathChain, holdingPosition, isBusy, isTurning, reachedParametricPathEnd, holdPositionAtEnd, manualDrive;
    private boolean automaticHoldEnd, useHoldScaling = true;
    private double globalMaxPower = 1, centripetalScaling;
    private double holdPointTranslationalScaling;
    private double holdPointHeadingScaling;
    private double turnHeadingErrorThreshold;
    private long reachedParametricPathEndTime;
    public boolean useTranslational = true;
    public boolean useCentripetal = true;
    public boolean useHeading = true;
    public boolean useDrive = true;
    public boolean usePredictiveBraking = true;
    private Timer zeroVelocityDetectedTimer = null;
    private Runnable resetFollowing = null;
    private Queue<PathCallback> currentCallbacks;

    /**
     * This creates a new Follower given a HardwareMap.
     * @param constants FollowerConstants to use
     * @param localizer Localizer to use
     * @param drivetrain Drivetrain to use
     * @param pathConstraints PathConstraints to use
     */
    public Follower(FollowerConstants constants, Localizer localizer, Drivetrain drivetrain, PathConstraints pathConstraints) {
        this(constants, localizer, drivetrain, pathConstraints, PedroUnits.DEFAULT);
    }

    /**
     * This creates a new Follower with an explicit user-interface unit configuration.
     * Pedro still calculates in inches, kilograms, and radians. After construction, TeamCode
     * pose, path, heading, and telemetry APIs use {@code units}.
     */
    public Follower(FollowerConstants constants, Localizer localizer, Drivetrain drivetrain, PathConstraints pathConstraints, PedroUnits units) {
        this.constants = constants;
        this.pathConstraints = pathConstraints;
        this.units = PedroUnits.requireNonNull(units);

        poseTracker = new PoseTracker(localizer);
        errorCalculator = new ErrorCalculator(constants);
        vectorCalculator = new VectorCalculator(constants);
        this.drivetrain = drivetrain;
        poseHistory = new PoseHistory(poseTracker);

        BEZIER_CURVE_SEARCH_LIMIT = constants.BEZIER_CURVE_SEARCH_LIMIT;
        holdPointTranslationalScaling = constants.holdPointTranslationalScaling;
        holdPointHeadingScaling = constants.holdPointHeadingScaling;
        centripetalScaling = constants.centripetalScaling;
        turnHeadingErrorThreshold = constants.turnHeadingErrorThreshold;
        automaticHoldEnd = constants.automaticHoldEnd;
        usePredictiveBraking = constants.usePredictiveBraking;

        breakFollowing();
    }


    public void updateConstants() {
        this.BEZIER_CURVE_SEARCH_LIMIT = constants.BEZIER_CURVE_SEARCH_LIMIT;
        this.holdPointTranslationalScaling = constants.holdPointTranslationalScaling;
        this.holdPointHeadingScaling = constants.holdPointHeadingScaling;
        this.centripetalScaling = constants.centripetalScaling;
        this.turnHeadingErrorThreshold = constants.turnHeadingErrorThreshold;
        this.automaticHoldEnd = constants.automaticHoldEnd;
        this.usePredictiveBraking = !manualDrive && constants.usePredictiveBraking;
    }

    /**
     * This creates a new Follower given a HardwareMap.
     * @param constants FollowerConstants to use
     * @param localizer Localizer to use
     * @param drivetrain Drivetrain to use
     */
    public Follower(FollowerConstants constants, Localizer localizer, Drivetrain drivetrain) {
        this(constants, localizer, drivetrain, PathConstraints.defaultConstraints);
    }

    public void setCentripetalScaling(double set) {
        centripetalScaling = set;
    }

    /**
     * This sets the maximum power the motors are allowed to use.
     *
     * @param set This caps the motor power from [0, 1].
     */
    public void setMaxPower(double set) {
        globalMaxPower = set;
        drivetrain.setMaxPowerScaling(set);
    }

    /**
     * This gets a Point from the current Path from a specified t-value, in configured units.
     *
     * @return returns the Point.
     */
    public Pose getPointFromPath(double t) {
        if (currentPath != null) {
            return units.toUserPose(currentPath.getPoint(t));
        } else {
            return null;
        }
    }

    /**
     * This sets the current pose in the PoseTracker without using offsets.
     * {@code pose} is in this follower's configured units.
     *
     * @param pose The pose to set the current pose to.
     */
    public void setPose(Pose pose) {
        poseTracker.setPose(units.toInternalPose(pose));
    }

    /**
     * This sets the current x-position estimate of the localizer in configured length units.
     * @param x the x-position estimate to set
     */
    public void setX(double x) {
        poseTracker.getLocalizer().setX(units.lengthToInternal(x));
    }

    /**
     * This sets the current y-position estimate of the localizer in configured length units.
     * @param y the y-position estimate to set
     */
    public void setY(double y) {
        poseTracker.getLocalizer().setY(units.lengthToInternal(y));
    }

    /**
     * This sets the current heading estimate of the localizer, in configured angle units.
     * @param heading the heading estimate to set
     */
    public void setHeading(double heading) {
        poseTracker.getLocalizer().setHeading(units.angleToInternal(heading));
    }

    /**
     * This returns the current pose in this follower's configured units.
     *
     * @return returns the pose
     */
    public Pose getPose() {
        return units.toUserPose(poseTracker.getPose());
    }

    /**
     * Canonical pose in inches and radians. Use for Panels Field drawing and other consumers
     * that still expect Pedro's internal coordinates.
     */
    public Pose getInternalPose() {
        return poseTracker.getPose();
    }

    /**
     * This returns the current velocity of the robot as a Vector in configured length units
     * per second. Vector direction remains radians.
     *
     * @return returns the current velocity as a Vector.
     */
    public Vector getVelocity() {
        return units.toUserVector(poseTracker.getVelocity());
    }

    /**
     * This sets the starting pose in configured units. Do not run this after moving at all.
     *
     * @param pose the pose to set the starting pose to.
     */
    public void setStartingPose(Pose pose) {
        poseTracker.setStartingPose(units.toInternalPose(pose));
    }

    /**
     * This holds a Point.
     *
     * @param point   the Point to stay at.
     * @param heading the heading to face.
     * @param useHoldScaling true if you want to correct and turn slowly, false otherwise
     */
    public void holdPoint(BezierPoint point, double heading, boolean useHoldScaling) {
        holdPointInternal(
                new BezierPoint(units.toInternalPose(point.getFirstControlPoint())),
                units.angleToInternal(heading),
                useHoldScaling);
    }

    private void holdPointInternal(BezierPoint internalPoint, double headingRadians, boolean useHoldScaling) {
        breakFollowing();
        holdingPosition = true;
        this.useHoldScaling = useHoldScaling;
        isBusy = false;
        followingPathChain = false;
        setPath(new Path(internalPoint));
        currentPath.setConstantHeadingInterpolation(headingRadians);
        previousClosestPose = closestPose;
        closestPose = currentPath.updateClosestPose(poseTracker.getPose(), 1);
    }


    /**
     * This holds a Point.
     *
     * @param point   the Point to stay at.
     * @param heading the heading to face.
     */
    public void holdPoint(BezierPoint point, double heading) {
        holdPoint(point, heading, true);
    }

    /**
     * This holds a Point.
     *
     * @param pose the Point (as a Pose) to stay at.
     */
    public void holdPoint(Pose pose) {
        holdPoint(pose, true);
    }

    /**
     * This holds a Point in configured units.
     *
     * @param pose the Point (as a Pose) to stay at.
     */
    public void holdPoint(Pose pose, boolean useHoldScaling) {
        Pose internal = units.toInternalPose(pose);
        holdPointInternal(new BezierPoint(internal), internal.getHeading(), useHoldScaling);
    }

    /**
     * This follows a Path.
     * This also makes the Follower hold the last Point on the Path.
     *
     * @param path the Path to follow.
     * @param holdEnd this makes the Follower hold the last Point on the Path.
     */
    public void followPath(Path path, boolean holdEnd) {
        drivetrain.setMaxPowerScaling(globalMaxPower);
        breakFollowing();
        holdPositionAtEnd = holdEnd;
        isBusy = true;
        followingPathChain = false;
        setPath(units.toInternalPath(path));
        previousClosestPose = closestPose;
        closestPose = currentPath.updateClosestPose(poseTracker.getPose(), BEZIER_CURVE_SEARCH_LIMIT);
    }

    /**
     * This follows a Path.
     *
     * @param path the Path to follow.
     */
    public void followPath(Path path) {
        followPath(path, automaticHoldEnd);
    }

    /**
     * This follows a PathChain. Drive vector projection is only done on the last Path.
     * This also makes the Follower hold the last Point on the PathChain.
     *
     * @param pathChain the PathChain to follow.
     * @param holdEnd this makes the Follower hold the last Point on the PathChain.
     */
    public void followPath(PathChain pathChain, boolean holdEnd) {
        followPath(pathChain, globalMaxPower, holdEnd);
    }

    /**
     * This follows a PathChain. Drive vector projection is only done on the last Path.
     *
     * @param pathChain the PathChain to follow.
     */
    public void followPath(PathChain pathChain) {
        followPath(pathChain, automaticHoldEnd);
    }

    /**
     * This follows a PathChain. Drive vector projection is only done on the last Path.
     * This also makes the Follower hold the last Point on the PathChain.
     *
     * @param pathChain the PathChain to follow.
     * @param maxPower the max power of the Follower for this path
     * @param holdEnd this makes the Follower hold the last Point on the PathChain.
     */
    public void followPath(PathChain pathChain, double maxPower, boolean holdEnd) {
        drivetrain.setMaxPowerScaling(maxPower);
        breakFollowing();
        holdPositionAtEnd = holdEnd;
        isBusy = true;
        followingPathChain = true;
        chainIndex = 0;
        currentPathChain = pathChain;
        setPath(pathChain.getPath(chainIndex));
        previousClosestPose = closestPose;
        closestPose = currentPath.updateClosestPose(poseTracker.getPose(), BEZIER_CURVE_SEARCH_LIMIT);
        currentPathChain.resetCallbacks();
        currentCallbacks = currentPathChain.getNextPathCallbacks(chainIndex);

        for (PathCallback callback : currentCallbacks) {
            callback.initialize();
        }
    }

    /**
     * Resumes pathing, can only be called after pausePathFollowing()
     */
    public void resumePathFollowing() {
        if (resetFollowing != null) {
            resetFollowing.run();
            resetFollowing = null;
            breakFollowing();
            isBusy = true;
            previousClosestPose = closestPose;
            closestPose = currentPath.updateClosestPose(poseTracker.getPose(), BEZIER_CURVE_SEARCH_LIMIT);
        }
    }

    /**
     * Pauses pathing, can only be restarted with resumePathFollowing
     */
    public void pausePathFollowing() {
        isBusy = false;

        boolean prevHoldEnd = holdPositionAtEnd;

        if (followingPathChain && currentPathChain != null) {
            PathChain lastChain = currentPathChain;
            int lastIndex = chainIndex;

            resetFollowing = () -> {
                followingPathChain = true;
                chainIndex = lastIndex;
                currentPathChain = lastChain;
                holdPositionAtEnd = prevHoldEnd;
                currentPath = currentPathChain.getPath(lastIndex);
            };
        } else if (currentPath != null) {
            Path lastPath = currentPath;

            resetFollowing = () -> {
                holdPositionAtEnd = prevHoldEnd;
                currentPath = lastPath;
            };
        }

        holdPoint(getPose());
    }

    /**
     * This starts teleop drive control.
     */
    public void startTeleopDrive() {
        breakFollowing();
        manualDrive = true;
        update();
        drivetrain.startTeleopDrive();
    }

    /**
     * This starts teleop drive control.
     */
    public void startTeleopDrive(boolean useBrakeMode) {
        breakFollowing();
        manualDrive = true;
        update();
        drivetrain.startTeleopDrive(useBrakeMode);
    }

    public void startTeleOpDrive(boolean useBrakeMode) {
        startTeleopDrive(useBrakeMode);
    }

    public void startTeleOpDrive() {
        startTeleopDrive();
    }

    /**
     * This sets the Teleop drive movement vectors
     *
     * @param forward the forward movement
     * @param strafe the strafe movement
     * @param turn the turn movement
     * @param isRobotCentric true if robot centric control, false if field centric
     * @param offsetHeading the offset heading for field centric control, will face the direction of such heading in radians in the field coordinate system when driving forward
     */
    public void setTeleOpDrive(double forward, double strafe, double turn, boolean isRobotCentric, double offsetHeading) {
        vectorCalculator.setTeleOpMovementVectors(forward, strafe, turn, isRobotCentric, units.angleToInternal(offsetHeading));
    }

    /**
     * This sets the Teleop drive movement vectors
     *
     * @param forward the forward movement
     * @param strafe the strafe movement
     * @param turn the turn movement
     * @param offsetHeading the offset heading for field centric control, in configured angle units
     */
    public void setTeleOpDrive(double forward, double strafe, double turn, double offsetHeading) {
        vectorCalculator.setTeleOpMovementVectors(forward, strafe, turn, true, units.angleToInternal(offsetHeading));
    }

    /**
     * This sets the Teleop drive movement vectors
     *
     * @param forward the forward movement
     * @param strafe the strafe movement
     * @param turn the turn movement
     * @param isRobotCentric true if robot centric control, false if field centric
     */
    public void setTeleOpDrive(double forward, double strafe, double turn, boolean isRobotCentric) {
        vectorCalculator.setTeleOpMovementVectors(forward, strafe, turn, isRobotCentric);
    }

    /**
     * This sets the Teleop drive movement vectors
     * This will default to robot centric control
     *
     * @param forward the forward movement
     * @param strafe the strafe movement
     * @param turn the turn movement
     */
    public void setTeleOpDrive(double forward, double strafe, double turn) {
        vectorCalculator.setTeleOpMovementVectors(forward, strafe, turn);
    }

    /** Updates the Mecanum constants */
    public void updateDrivetrain() {
        drivetrain.updateConstants();
    }

    /** Calls an update to the PoseTracker, which updates the robot's current position estimate. */
    public void updatePose() {
        poseTracker.update();
        currentPose = poseTracker.getPose();
        poseHistory.update();
    }

    /** Calls an update to the ErrorCalculator, which updates the robot's current error. */
    public void updateErrors() {
        errorCalculator.update(currentPose, currentPath, currentPathChain, followingPathChain, closestPose.getPose(), poseTracker.getVelocity(), chainIndex, drivetrain.xVelocity(), drivetrain.yVelocity(), getClosestPointHeadingGoal(), usePredictiveBraking);
    }

    /** Calls an update to the VectorCalculator, which updates the robot's current vectors to correct. */
    public void updateVectors() {
        vectorCalculator.update(useDrive, useHeading, useTranslational, useCentripetal,
                                manualDrive, chainIndex,
                                drivetrain.getMaxPowerScaling(), followingPathChain,
                                centripetalScaling, currentPose, closestPose.getPose(),
                                poseTracker.getVelocity(), currentPath,
                                currentPathChain, useDrive && !holdingPosition ?
                                    errorCalculator.getDriveError() : -1, errorCalculator.getTranslationalError(),
                                errorCalculator.getHeadingError(), getClosestPointHeadingGoal(),
                                internalTotalDistanceRemaining(), usePredictiveBraking);
    }

    public void updateErrorAndVectors() {updateErrors(); updateVectors();}


    /**
     * This calls an update to the PoseTracker, which updates the robot's current position estimate.
     * This also updates all the Follower's PIDFs, which updates the motor powers.
     */
    public void update() {
        poseHistory.update();
        updateConstants();
        updatePose();
        updateDrivetrain();


        if (manualDrive) {
            previousClosestPose = closestPose;
            closestPose = new PathPoint();
            updateErrorAndVectors();
            drivetrain.runDrive(getCentripetalForceCorrection(), getTeleopHeadingVector(), getTeleopDriveVector(), poseTracker.getPose().getHeading(), poseTracker.getVelocity());
            return;
        }

        if (currentPath == null) {
            return;
        }

        if (holdingPosition) {
            previousClosestPose = closestPose;
            if (followingPathChain) currentPathChain.update();
            closestPose = currentPath.updateClosestPose(poseTracker.getPose(), 1);
            updateErrorAndVectors();
            drivetrain.runDrive(useHoldScaling? getTranslationalCorrection().times(holdPointTranslationalScaling) : getTranslationalCorrection(), useHoldScaling? getHeadingVector().times(holdPointHeadingScaling) : getHeadingVector(), new Vector(), poseTracker.getPose().getHeading(), poseTracker.getVelocity());

            if(Math.abs(errorCalculator.getHeadingError()) < turnHeadingErrorThreshold && isTurning) {
                isTurning = false;
                isBusy = false;
            }
            return;
        }

        if (isBusy) {
            previousClosestPose = closestPose;
            if (followingPathChain) currentPathChain.update();
            closestPose = currentPath.updateClosestPose(poseTracker.getPose(), BEZIER_CURVE_SEARCH_LIMIT);
            updateErrorAndVectors();
            if (followingPathChain) updateCallbacks();
            drivetrain.runDrive(getCorrectiveVector(), getHeadingVector(), getDriveVector(), poseTracker.getPose().getHeading(), poseTracker.getVelocity());
        }

        if (poseTracker.getVelocity().getMagnitude() < constants.stuckVelocity && zeroVelocityDetectedTimer == null && isBusy &&
               currentPath.getClosestPointTValue() > constants.stuckTValueLow && currentPath.getClosestPointTValue() < constants.stuckTValueHigh) {
            zeroVelocityDetectedTimer = new Timer();
        }
        
        boolean nextPathWithinBrakingDistance =
            followingPathChain && chainIndex < currentPathChain.size() - 1 && usePredictiveBraking
                && vectorCalculator.driveVector.dot(getClosestPointTangentVector()) < 1;
        
        if (!(currentPath.isAtParametricEnd()
              //|| nextPathWithinBrakingDistance
                || (zeroVelocityDetectedTimer != null
                && zeroVelocityDetectedTimer.getElapsedTime() > constants.stuckTimeout))) {
            return;
        }

        if (followingPathChain && chainIndex < currentPathChain.size() - 1) {
            breakFollowing();
            isBusy = true;
            followingPathChain = true;
            chainIndex++;
            setPath(currentPathChain.getPath(chainIndex));
            previousClosestPose = closestPose;
            if (followingPathChain) currentPathChain.update();
            closestPose = currentPath.updateClosestPose(poseTracker.getPose(), BEZIER_CURVE_SEARCH_LIMIT);
            updateErrorAndVectors();
            currentCallbacks = currentPathChain.getNextPathCallbacks(chainIndex);

            for (PathCallback callback : currentCallbacks) {
                callback.initialize();
            }

            return;
        }

        if (!reachedParametricPathEnd) {
            reachedParametricPathEnd = true;
            reachedParametricPathEndTime = System.currentTimeMillis();
        }

        updateErrorAndVectors();
        if (!(
            (
                System.currentTimeMillis() - reachedParametricPathEndTime
                > currentPath.getPathEndTimeoutConstraint()
            )
            || (
                poseTracker.getVelocity().getMagnitude()
                < currentPath.getPathEndVelocityConstraint()
            )
            && (
                poseTracker.getPose().distanceFrom(closestPose.getPose())
                < currentPath.getPathEndTranslationalConstraint()
            )
            && (
                MathFunctions.getSmallestAngleDifference(poseTracker.getPose().getHeading(), getClosestPointHeadingGoal())
                < currentPath.getPathEndHeadingConstraint()
            )
        )) {
            return;
        }

        if (holdPositionAtEnd) {
            holdPositionAtEnd = false;
            if (followingPathChain) holdPoint(new BezierPoint(currentPath.getLastControlPoint()), currentPathChain.getHeadingGoal(new PathChain.PathT(currentPathChain.size() - 1, 1)));
            else holdPoint(new BezierPoint(currentPath.getLastControlPoint()), currentPath.getHeadingGoal(1));
        } else {
            breakFollowing();
        }
    }

    /** This checks if any PathCallbacks should be run right now, and runs them if applicable. */
    public void updateCallbacks() {
        for (PathCallback callback : currentCallbacks) {
            if (callback.isReady()) {
                callback.run();
            }
        }
    }

    /** This resets the PIDFs and stops following the current Path. */
    public void breakFollowing() {
        errorCalculator.breakFollowing();
        vectorCalculator.breakFollowing();
        drivetrain.breakFollowing();
        manualDrive = false;
        holdingPosition = false;
        isBusy = false;
        isTurning = false;
        reachedParametricPathEnd = false;
        zeroVelocityDetectedTimer = null;
    }

    /**
     * This returns if the Follower is currently following a Path or a PathChain.
     * @return returns if the Follower is busy.
     */
    public boolean isBusy() {
        return isBusy;
    }

    /**
     * This returns the closest pose to the robot on the Path the Follower is currently following.
     * This closest pose is calculated through a binary search method with some specified number of
     * steps to search. By default, 10 steps are used, which should be more than enough.
     * The target heading associated with the pose is returned here as well.
     * @return returns the closest pose.
     */
    public PathPoint getClosestPose() {
        return closestPose;
    }

    /**
     * This returns whether the follower is at the parametric end of its current Path.
     * The parametric end is determined by if the closest Point t-value is greater than some specified
     * end t-value.
     * If running a PathChain, this returns true only if at parametric end of last Path in the PathChain.
     * @return returns whether the Follower is at the parametric end of its Path.
     */
    public boolean atParametricEnd() {
        if (currentPath == null){
            return true;
        }

        if (followingPathChain) {
            if (chainIndex == currentPathChain.size() - 1) return currentPath.isAtParametricEnd();
            return false;
        }
        return currentPath.isAtParametricEnd();
    }

    /**
     * This returns the t value of the closest point on the current Path to the robot
     * In the absence of a current Path, it returns 1.0.
     * @return returns the current t value.
     */
    public double getCurrentTValue() {
        if (isBusy) return currentPath.getClosestPointTValue();
        return 1.0;
    }

    /**
     * This returns the current path number. For following Paths, this will return 0. For PathChains,
     * this will return the current path number. For holding Points, this will also return 0.
     * @return returns the current path number.
     */
    public double getCurrentPathNumber() {
        if (!followingPathChain) return 0;
        return chainIndex;
    }

    /**
     * This returns a new PathBuilder object for easily building PathChains.
     * @return returns a new PathBuilder object.
     */
    public PathBuilder pathBuilder(PathConstraints constraints) {
        return new PathBuilder(this, constraints);
    }

    /**
     * This returns a new PathBuilder object for easily building PathChains.
     * @return returns a new PathBuilder object.
     */
    public PathBuilder pathBuilder() {
        return new PathBuilder(this);
    }

    /**
     * This returns the total heading the robot has turned, in configured angle units.
     * @return the total heading.
     */
    public double getTotalHeading() {
        return units.angleFromInternal(poseTracker.getTotalHeading());
    }

    /**
     * This returns the current Path the Follower is following. This can be null.
     * @return returns the current Path.
     */
    public Path getCurrentPath() {
        return currentPath;
    }

    //Thanks to team 21229 Quality Control for creating this algorithm to detect if the robot is stuck.
    /** @return true if the robot is stuck and false otherwise */
    public boolean isRobotStuck() {
        return zeroVelocityDetectedTimer != null;
    }

    public boolean isLocalizationNAN() {
        return poseTracker.getLocalizer().isNAN();
    }

    /** Turns by a heading amount in configured angle units.
     * @param headingDelta the amount to turn
     * @param counterClockwise true if turning counterclockwise, false if turning clockwise
     */
    public void turn(double headingDelta, boolean counterClockwise) {
        double radians = units.angleToInternal(headingDelta);
        Pose internal = poseTracker.getPose();
        holdPointInternal(
                new BezierPoint(new Pose(internal.getX(), internal.getY(), internal.getHeading() + (counterClockwise ? radians : -radians))),
                internal.getHeading() + (counterClockwise ? radians : -radians),
                false);
        isTurning = true;
        isBusy = true;
    }

    /** Turns by a heading amount in configured angle units, counterclockwise positive.
     * @param headingDelta the amount to turn
     */
    public void turn(double headingDelta) {
        turn(headingDelta, true);
    }


    /** Turns to a specific heading in configured angle units.
     * @param heading the heading to turn to
     */
    public void turnTo(double heading) {
        double target = units.angleToInternal(heading);
        Pose internal = poseTracker.getPose();
        double resolved = MathFunctions.normalizeAngleSigned(internal.getHeading() + MathFunctions.getSmallestAngleDifference(internal.getHeading(), target));
        holdPointInternal(new BezierPoint(new Pose(internal.getX(), internal.getY(), resolved)), resolved, false);
        isTurning = true;
        isBusy = true;
    }

    /** Turns to a specific heading in degrees
     * @param degrees the heading in degrees to turn to
     */
    @Deprecated
    public void turnToDegrees(double degrees) {
        Pose internal = poseTracker.getPose();
        double radians = Math.toRadians(degrees);
        double resolved = MathFunctions.normalizeAngleSigned(internal.getHeading() + MathFunctions.getSmallestAngleDifference(internal.getHeading(), radians));
        holdPointInternal(new BezierPoint(new Pose(internal.getX(), internal.getY(), resolved)), resolved, false);
        isTurning = true;
        isBusy = true;
    }

    /** Turns a certain amount of degrees left
     * @param degrees the amount of degrees to turn
     * @param isLeft true if turning left, false if turning right
     */
    @Deprecated
    public void turnDegrees(double degrees, boolean isLeft) {
        Pose internal = poseTracker.getPose();
        double radians = Math.toRadians(degrees);
        holdPointInternal(
                new BezierPoint(new Pose(internal.getX(), internal.getY(), internal.getHeading() + (isLeft ? radians : -radians))),
                internal.getHeading() + (isLeft ? radians : -radians),
                false);
        isTurning = true;
        isBusy = true;
    }

    public boolean isTurning() {
        return isTurning;
    }

    /**
     * Checks if the robot is at a certain pose within certain tolerances.
     * The pose and tolerances are in this follower's configured units.
     * @param pose Pose to compare with the current pose
     * @param xTolerance Tolerance for the x position
     * @param yTolerance Tolerance for the y position
     * @param headingTolerance Tolerance for the heading
     */
    public boolean atPose(Pose pose, double xTolerance, double yTolerance, double headingTolerance) {
        return Math.abs(pose.getX() - getPose().getX()) < xTolerance && Math.abs(pose.getY() - getPose().getY()) < yTolerance && Math.abs(pose.getHeading() - getPose().getHeading()) < headingTolerance;
    }

    /**
     * Checks if the robot is at a certain pose within certain tolerances
     * @param pose Pose to compare with the current pose
     * @param xTolerance Tolerance for the x position
     * @param yTolerance Tolerance for the y position
     */
    public boolean atPose(Pose pose, double xTolerance, double yTolerance) {
        return Math.abs(pose.getX() - getPose().getX()) < xTolerance && Math.abs(pose.getY() - getPose().getY()) < yTolerance;
    }

    /**
     * Sets the maximum power that can be used by the Drivetrain.
     * @param maxPowerScaling setting the max power scaling
     */
    public void setMaxPowerScaling(double maxPowerScaling) {
        drivetrain.setMaxPowerScaling(maxPowerScaling);
    }

    /**
     * Gets the maximum power that can be used by the drive vector scaler. Ranges between 0 and 1.
     * @return returns the max power scaling
     */
    public double getMaxPowerScaling() {
        return drivetrain.getMaxPowerScaling();
    }

    /** Returns the useDrive boolean */
    public boolean getUseDrive() { return useDrive; }

    /** Returns the useHeading boolean */
    public boolean getUseHeading() { return useHeading; }

    /** Returns the useTranslational boolean */
    public boolean getUseTranslational() { return useTranslational; }

    /** Returns the useCentripetal boolean */
    public boolean getUseCentripetal() { return useCentripetal; }

    /** Return the teleopDrive boolean */
    public boolean getTeleopDrive() { return manualDrive; }

    /** Returns the chainIndex of the current PathChain */
    public int getChainIndex() { return chainIndex; }

    /** Returns the current PathChain */
    public PathChain getCurrentPathChain() { return currentPathChain; }

    /** Returns if following a path chain */
    public boolean getFollowingPathChain() { return followingPathChain; }

    /** Return the centripetal scaling */
    public double getCentripetalScaling() { return centripetalScaling; }

    /**
     * This returns whether the Follower is currently in teleop drive mode.
     * @return returns true if in teleop drive mode, false otherwise.
     */
    public boolean isTeleopDrive() { return manualDrive; }

    /**
     * This returns the teleop heading vector, which is the vector that the robot should be heading towards
     * @return returns the teleop heading vector
     */
    public Vector getTeleopHeadingVector() { return vectorCalculator.getTeleopHeadingVector(); }

    /**
     * This returns the teleop drive vector, which is the vector that the robot should be driving towards
     * @return returns the teleop drive vector
     */
    public Vector getTeleopDriveVector() { return vectorCalculator.getTeleopDriveVector(); }

    /**
     * This returns the heading error in configured angle units.
     * @return returns the heading error
     */
    public double getHeadingError() { return units.angleFromInternal(errorCalculator.getHeadingError()); }

    /**
     * This returns the translational error in configured length units.
     * @return returns the translational error as a Vector.
     */
    public Vector getTranslationalError() { return units.toUserVector(errorCalculator.getTranslationalError()); }

    /**
     * This returns the drive error in configured length units per second.
     * @return The drive error as a double.
     */
    public double getDriveError() { return units.velocityFromInternal(errorCalculator.getDriveError()); }

    /**
     * This returns the drive vector, which is the vector that the robot should be moving towards to reach the closest point on the Path.
     * @return returns the drive vector
     */
    public Vector getDriveVector() { return vectorCalculator.getDriveVector(); }

    /**
     * This returns the corrective vector, which is the vector that the robot should be moving towards to correct its position.
     * @return returns the corrective vector
     */
    public Vector getCorrectiveVector() { return vectorCalculator.getCorrectiveVector(); }

    /**
     * This returns the heading vector, which is the vector that the robot should be heading towards to reach the closest point on the Path.
     * @return returns the heading vector
     */
    public Vector getHeadingVector() { return vectorCalculator.getHeadingVector(); }

    /**
     * This returns the translational correction, which is the vector that the robot should be moving towards to correct its position.
     * @return returns the translational correction
     */
    public Vector getTranslationalCorrection() { return vectorCalculator.getTranslationalCorrection(); }

    /**
     * This returns the centripetal force correction, which is the vector that the robot should be moving towards to correct its position for centripetal force.
     * @return returns the centripetal force correction
     */
    public Vector getCentripetalForceCorrection() { return vectorCalculator.getCentripetalForceCorrection(); }

    /**
     * This returns the PoseHistory, which is a history of the robot's poses.
     * @return returns the PoseHistory
     */
    public PathConstraints getConstraints() { return pathConstraints; }

    /**
     * The immutable user-interface unit configuration selected for this follower.
     */
    public PedroUnits getUnits() {
        return units;
    }

    /**
     * Create a {@link Pose} in this follower's configured units. The stored numbers are those units;
     * pass the pose to follower and path-builder APIs.
     */
    public Pose pose(double x, double y, double heading) {
        return units.pose(x, y, heading);
    }

    /**
     * Create a {@link Pose} in this follower's configured length units with heading 0.
     */
    public Pose pose(double x, double y) {
        return units.pose(x, y);
    }

    /**
     * Present the current pose in this follower's configured units with unit symbols.
     */
    public ConfiguredPose getPoseInConfiguredUnits() {
        return units.fromInternalPose(getInternalPose());
    }

    /**
     * This returns the FollowerConstants used by the Follower.
     * PID coefficients on this object are unitless. Prefer {@link #getMass()},
     * {@link #getForwardZeroPowerAcceleration()}, and {@link #getLateralZeroPowerAcceleration()}
     * for physical quantities in configured units.
     */
    public FollowerConstants getConstants() { return constants; }

    /**
     * Robot mass in configured mass units.
     */
    public double getMass() {
        return units.massFromInternal(constants.mass);
    }

    /**
     * Robot mass in configured mass units.
     */
    public void setMass(double mass) {
        constants.mass(units.massToInternal(mass));
    }

    /**
     * Forward zero-power acceleration in configured length units per second squared.
     */
    public double getForwardZeroPowerAcceleration() {
        return units.accelerationFromInternal(constants.forwardZeroPowerAcceleration);
    }

    /**
     * Forward zero-power acceleration in configured length units per second squared.
     */
    public void setForwardZeroPowerAcceleration(double acceleration) {
        constants.setForwardZeroPowerAcceleration(units.accelerationToInternal(acceleration));
    }

    /**
     * Lateral zero-power acceleration in configured length units per second squared.
     */
    public double getLateralZeroPowerAcceleration() {
        return units.accelerationFromInternal(constants.lateralZeroPowerAcceleration);
    }

    /**
     * Lateral zero-power acceleration in configured length units per second squared.
     */
    public void setLateralZeroPowerAcceleration(double acceleration) {
        constants.setLateralZeroPowerAcceleration(units.accelerationToInternal(acceleration));
    }

    /**
     * Path completion velocity in configured length units per second.
     */
    public double getPathCompletionVelocity() {
        return units.velocityFromInternal(pathConstraints.getVelocityConstraint());
    }

    /**
     * Forward ticks-to-distance multiplier in configured length units per tick.
     */
    public double getForwardMultiplier() {
        return units.lengthFromInternal(poseTracker.getLocalizer().getForwardMultiplier());
    }

    /**
     * Lateral ticks-to-distance multiplier in configured length units per tick.
     */
    public double getLateralMultiplier() {
        return units.lengthFromInternal(poseTracker.getLocalizer().getLateralMultiplier());
    }

    /**
     * Turning ticks-to-angle multiplier in configured angle units per tick.
     */
    public double getTurningMultiplier() {
        return units.angleFromInternal(poseTracker.getLocalizer().getTurningMultiplier());
    }

    /**
     * This sets the PathConstraints for the Follower.
     * @param pathConstraints the PathConstraints to set
     */
    public void setConstraints(PathConstraints pathConstraints) { this.pathConstraints = pathConstraints; }

    /**
     * This returns the Drivetrain used by the Follower.
     * @return returns the Drivetrain
     */
    public Drivetrain getDrivetrain() { return drivetrain; }

    /**
     * This returns the PoseTracker used by the Follower.
     * @return returns the PoseTracker
     */
    public PoseTracker getPoseTracker() { return poseTracker; }

    /**
     * This returns the ErrorCalculator used by the Follower.
     * @return returns the ErrorCalculator
     */
    public ErrorCalculator getErrorCalculator() { return errorCalculator; }

    /**
     * This returns the VectorCalculator used by the Follower.
     * @return returns the VectorCalculator
     */
    public VectorCalculator getVectorCalculator() { return vectorCalculator; }

    /**
     * This returns the PoseHistory used by the Follower.
     * @return returns the PoseHistory
     */
    public PoseHistory getPoseHistory() { return poseHistory; }

    /**
     * This sets the x movement of the drivetrain in configured length units per second.
     * @param vel the x movement to set
     */
    public void setXVelocity(double vel) { drivetrain.setXVelocity(units.velocityToInternal(vel)); }

    /**
     * Forward maximum velocity in configured length units per second.
     */
    public double getXVelocity() {
        return units.velocityFromInternal(drivetrain.xVelocity());
    }

    /**
     * This sets the y velocity of the drivetrain in configured length units per second.
     * @param vel the y velocity to set
     */
    public void setYVelocity(double vel) { drivetrain.setYVelocity(units.velocityToInternal(vel)); }

    /**
     * Lateral maximum velocity in configured length units per second.
     */
    public double getYVelocity() {
        return units.velocityFromInternal(drivetrain.yVelocity());
    }

    /**
     * This sets the Drive PIDF coefficients for the Follower.
     * @param drivePIDFCoefficients the Drive PIDF coefficients to set
     */
    public void setDrivePIDFCoefficients(FilteredPIDFCoefficients drivePIDFCoefficients) { vectorCalculator.setDrivePIDFCoefficients(drivePIDFCoefficients); }

    /**
     * This sets the Secondary Drive PIDF coefficients for the Follower.
     * @param secondaryDrivePIDFCoefficients the Secondary Drive PIDF coefficients to set
     */
    public void setSecondaryDrivePIDFCoefficients(FilteredPIDFCoefficients secondaryDrivePIDFCoefficients) { vectorCalculator.setSecondaryDrivePIDFCoefficients(secondaryDrivePIDFCoefficients); }

    /**
     * This sets the Heading PIDF coefficients for the Follower.
     * @param headingPIDFCoefficients the Heading PIDF coefficients to set
     */
    public void setHeadingPIDFCoefficients(PIDFCoefficients headingPIDFCoefficients) { vectorCalculator.setHeadingPIDFCoefficients(headingPIDFCoefficients); }

    /**
     * This sets the Secondary Heading PIDF coefficients for the Follower.
     * @param secondaryHeadingPIDFCoefficients the Secondary Heading PIDF coefficients to set
     */
    public void setSecondaryHeadingPIDFCoefficients(PIDFCoefficients secondaryHeadingPIDFCoefficients) { vectorCalculator.setSecondaryHeadingPIDFCoefficients(secondaryHeadingPIDFCoefficients); }

    /**
     * This sets the Translational PIDF coefficients for the Follower.
     * @param translationalPIDFCoefficients the Translational PIDF coefficients to set
     */
    public void setTranslationalPIDFCoefficients(PIDFCoefficients translationalPIDFCoefficients) { vectorCalculator.setTranslationalPIDFCoefficients(translationalPIDFCoefficients); }

    /**
     * This sets the Secondary Translational PIDF coefficients for the Follower.
     * @param secondaryTranslationalPIDFCoefficients the Secondary Translational PIDF coefficients to set
     */
    public void setSecondaryTranslationalPIDFCoefficients(PIDFCoefficients secondaryTranslationalPIDFCoefficients) { vectorCalculator.setSecondaryTranslationalPIDFCoefficients(secondaryTranslationalPIDFCoefficients); }

    /**
     * This sets the FollowerConstants for the Follower.
     * @param constants the FollowerConstants to set
     */
    public void setConstants(FollowerConstants constants) {
        this.constants = constants;
        updateConstants();
        errorCalculator.setConstants(constants);
        vectorCalculator.setConstants(constants);
        drivetrain.updateConstants();
    }

    /**
     * This returns the heading goal at a specific t-value.
     * @param t the t-value to get the heading goal at
     * @return returns the heading goal at the specified t-value
     */
    public double getHeadingGoal(double t) {
        if (currentPathChain != null) {
            return currentPathChain.getHeadingGoal(new PathChain.PathT(chainIndex, t));
        }

        return currentPath.getHeadingGoal(t);
    }

    /**
     * This returns the heading goal at a specific PathPoint.
     * @param point the PathPoint to get the heading goal at
     * @return returns the heading goal at the specified PathPoint
     */
    private double getHeadingGoal(PathPoint point) {
        if (currentPath == null) return 0;
        if (currentPathChain != null) return currentPathChain.getHeadingGoal(new PathChain.PathT(chainIndex, point.tValue));
        return currentPath.getHeadingGoal(point);
    }

    /**
     * This returns the closest point's heading goal
     * @return returns the closest point's heading goal
     */
    public double getClosestPointHeadingGoal() {
        if (currentPath == null) return 0;
        if (followingPathChain && currentPathChain != null)
            return currentPathChain.getClosestPointHeadingGoal(new PathChain.PathT(chainIndex, closestPose.tValue));
        return currentPath.getHeadingGoal(closestPose);
    }

    /**
     * This returns the closest point's tangent vector.
     * @return returns the closest point's tangent vector
     */
    public Vector getClosestPointTangentVector() {
        return getClosestPose().getTangentVector();
    }

    /**
     * This activates all the PIDFs used by the Follower.
     * This is useful for debugging and testing purposes.
     */
    public void activateAllPIDFs() {
        useDrive = true;
        useHeading = true;
        useTranslational = true;
        useCentripetal = true;
    }

    /**
     * This deactivates all the PIDFs used by the Follower.
     * This is useful for debugging and testing purposes.
     */
    public void deactivateAllPIDFs() {
        useDrive = false;
        useHeading = false;
        useTranslational = false;
        useCentripetal = false;
    }

    /**
     * This activates the Drive PIDF.
     * This is useful for debugging and testing purposes.
     */
    public void activateDrive() { useDrive = true; }

    /**
     * This activates the Heading PIDF.
     * This is useful for debugging and testing purposes.
     */
    public void activateHeading() { useHeading = true; }

    /**
     * This activates the Translational PIDF.
     * This is useful for debugging and testing purposes.
     */
    public void activateTranslational() { useTranslational = true; }

    /**
     * This activates the Centripetal PIDF.
     * This is useful for debugging and testing purposes.
     */
    public void activateCentripetal() { useCentripetal = true; }

    /**
     * This gets the distance traveled on the current Path.
     * @return returns the distance traveled on the current Path.
     */
    public double getDistanceTraveledOnPath() {
        if (currentPath == null) {
            return 0;
        }
        return units.lengthFromInternal(currentPath.getDistanceTraveled());
    }

    /**
     * This gets the proportion of the current Path that has been completed.
     * @return returns the proportion of the current Path that has been completed.
     */
    public double getPathCompletion() {
        if (currentPath == null) {
            return 0;
        }
        return currentPath.getPathCompletion();
    }

    /**
     * This gets the distance remaining on the current Path.
     * @return returns the distance remaining on the current Path.
     */
    public double getDistanceRemaining() {
        if (currentPath == null) {
            return 0;
        }
        return units.lengthFromInternal(currentPath.getDistanceRemaining());
    }

    /**
     * This is a debugging method that returns a String array of debug information.
     */
    public String[] debug() {
        String[] info = new String[4];
        info[0] = poseTracker.debugString();
        info[1] = errorCalculator.debugString();
        info[2] = vectorCalculator.debugString();
        info[3] = drivetrain.debugString();
        return info;
    }

    /**
     * This returns the acceleration of the robot.
     * @return returns the acceleration as a Vector.
     */
    public Vector getAcceleration() {
        return units.toUserVector(poseTracker.getAcceleration());
    }

    /**
     * This returns the angular velocity of the robot in configured angle units per second.
     * @return returns the angular velocity as a double.
     */
    public double getAngularVelocity() {
        return units.angleFromInternal(poseTracker.getAngularVelocity());
    }

    private void setPath(Path path) {
        this.currentPath = path;
        currentPath.init();
    }

    public PathPoint getPreviousClosestPose() {
        return previousClosestPose;
    }

    /**
     * This gets the tangential velocity of the robot along the path
     * @return the tangential velocity of the robot
     */
    public double getTangentialVelocity() {
        return units.velocityFromInternal(poseTracker.getVelocity().dot(getClosestPointTangentVector().normalize()));
    }

    public double getHeading() {
        return getPose().getHeading();
    }

    /**
     * Gets the total distance remaining for the robot to follow along the entire PathChain,
     * in configured length units.
     * @return the distance left on the current PathChain to follow
     */
    public double getTotalDistanceRemaining() {
        return convertDistanceRemaining(internalTotalDistanceRemaining());
    }

    private double internalTotalDistanceRemaining() {
        if (currentPath == null) {
            return 0;
        }

        if (!followingPathChain) {
            return currentPath.getDistanceRemaining();
        }
        
        PathChain.DecelerationType type = currentPathChain.getDecelerationType();
        if (type == PathChain.DecelerationType.NONE) {
            return -1;
        }
        
        return currentPathChain.getDistanceRemaining(chainIndex);
    }

    private double convertDistanceRemaining(double inches) {
        if (inches < 0) {
            return inches;
        }
        return units.lengthFromInternal(inches);
    }
}
