# Pedro Pathing (Allsparks fork)

Fork of [Pedro-Pathing/PedroPathing](https://github.com/Pedro-Pathing/PedroPathing) used by **[The Allsparks](https://github.com/The-Allsparks)** (FTC Team **36117**).

Upstream docs and tuning: [pedropathing.com](https://pedropathing.com/). Discord: [Pedro Pathing](https://discord.gg/2GfC4qBP5s).

> **Disclaimer:** This is a community fork. It is **not** affiliated with or endorsed by Pedro Pathing, FIRST, or other referenced vendors. Keep the BSD 3-Clause license. Merge upstream `main` regularly.

## Why this fork exists

Upstream Pedro calculates in **inches**, **kilograms**, and **radians**. This fork adds a TeamCode-facing unit conversion layer so a team can enter and display values in their preferred units without changing Pedro’s internal math.

`.setUnits(...)` selects those interface units once per `FollowerBuilder`. Existing Pedro APIs keep their original canonical meanings.

## Canonical internal units

Pedro always calculates in:

* Distance: inches
* Velocity: inches per second
* Acceleration: inches per second squared
* Mass: kilograms
* Angles: radians
* Angular velocity: radians per second
* Existing timeout/time units unchanged

`.setUnits(...)` does **not** rescale PID coefficients, covariance, drivetrain defaults, path constraints, poses, or follower math.

## Supported interface units

Length: `INCHES`, `FEET`, `CENTIMETERS`, `METERS`

Mass: `KILOGRAMS`, `POUNDS`

Angles: `RADIANS`, `DEGREES` (`AngularUnit` in core, to avoid colliding with the FTC SDK `AngleUnit`)

Millimeters are **not** a selectable interface unit. They may be used internally by hardware adapters such as OctoQuad.

## Two API layers

### Existing Pedro API (canonical)

These keep their original meanings even after `.setUnits(...)`:

```java
new Pose(24, 48, Math.PI / 2); // 24 in, 48 in, π/2 rad
constants.mass(10.65);         // kilograms
constants.forwardZeroPowerAcceleration(-41.278); // in/s^2
mecanumConstants.xVelocity(81.34); // in/s
pose.getX();                   // inches
pose.getHeading();             // radians
```

### Configured interface API

New helpers on `FollowerBuilder`, `Follower`, and `PedroUnits` interpret numbers using the units selected once by `.setUnits(...)`. You do not repeat the unit on every call.

```java
Follower follower = new FollowerBuilder(constants, hardwareMap)
        .setUnits(LengthUnit.FEET, MassUnit.POUNDS, AngularUnit.DEGREES)
        .setMass(23.5)
        .setStartingPose(2, 4, 90)
        .pinpointLocalizer(pinpointConstants)
        .mecanumDrivetrain(mecanumConstants)
        .build();
```

Internally that becomes approximately 10.659 kg, 24 inches, 48 inches, and π/2 radians.

## Basic setup

If you never call `.setUnits(...)`, behavior matches upstream Pedro (inches, kilograms, radians):

```java
Follower follower = new FollowerBuilder(constants, hardwareMap)
        .pinpointLocalizer(pinpointConstants)
        .mecanumDrivetrain(mecanumConstants)
        .build();
```

Call `.setUnits(...)` immediately after constructing the builder, at most once:

```java
PedroUnits units = new PedroUnits(
        LengthUnit.FEET,
        MassUnit.POUNDS,
        AngularUnit.DEGREES
);

Follower follower = new FollowerBuilder(constants, hardwareMap)
        .setUnits(units)
        .build();
```

Overloads:

```java
FollowerBuilder setUnits(PedroUnits units);

FollowerBuilder setUnits(LengthUnit lengthUnit, MassUnit massUnit, AngularUnit angleUnit);

FollowerBuilder setUnits(LengthUnit lengthUnit); // mass kg, angle rad

FollowerBuilder setUnits(LengthUnit lengthUnit, AngularUnit angleUnit); // mass kg
```

`PedroUnits.DEFAULT` is inches, kilograms, and radians. Calling `.setUnits(PedroUnits.DEFAULT)` explicitly is valid. A second call throws `IllegalStateException`.

## Configured pose construction and output

```java
Pose start = follower.pose(2, 4, 90);
Pose target = follower.getUnits().pose(5, 3, 180);
follower.setStartingPose(start);
```

Those `Pose` objects store canonical inches and radians, so they are safe to pass through existing Pedro path APIs.

Do not put feet or degrees into a standard `Pose`. Configured output uses a distinct type:

```java
ConfiguredPose display = follower.getPoseInConfiguredUnits();
double xFeet = display.x();
double headingDeg = display.heading();

double x = follower.getUnits().x(follower.getPose());
ConfiguredPose same = follower.getUnits().fromInternalPose(follower.getPose());
```

`ConfiguredPose` is not a Pedro `Pose` and must not be passed into follower math.

## Path constraints

`PathConstraints` still stores inches per second, inches, and radians. Build them from configured units:

```java
PathConstraints constraints = follower.getUnits()
        .pathConstraints()
        .velocityConstraint(0.5)
        .translationalConstraint(0.05)
        .headingConstraint(2)
        .build();
```

With feet/degrees configured, that produces an ordinary canonical `PathConstraints`. Creating one follower does not mutate `PathConstraints.defaultConstraints`.

## Hardware units are independent

`.setUnits(...)` does not overwrite Pinpoint `distanceUnit` or OTOS `linearUnit`.

User-facing feet/degrees → Pedro inches/radians → hardware millimeters (or whatever the device is configured to use). Each boundary converts once.

**Pinpoint in millimeters, user-facing feet**

```java
new FollowerBuilder(constants, hardwareMap)
        .setUnits(LengthUnit.FEET, MassUnit.POUNDS, AngularUnit.DEGREES)
        .pinpointLocalizer(new PinpointConstants().distanceUnit(DistanceUnit.MM))
        .build();
```

Pod offsets stay in Pinpoint’s hardware unit.

**OTOS in inches, user-facing centimeters**

```java
new FollowerBuilder(constants, hardwareMap)
        .setUnits(LengthUnit.CENTIMETERS, AngularUnit.DEGREES)
        .OTOSLocalizer(new OTOSConstants().linearUnit(DistanceUnit.INCH))
        .build();
```

The OTOS sensor offset stays in the OTOS linear unit.

**OctoQuad, user-facing feet, hardware millimeters**

```java
new FollowerBuilder(constants, hardwareMap)
        .setUnits(LengthUnit.FEET)
        .octoQuadLocalizer(octoQuadConstants, OctoQuadLocalizer.InitMode.INITIALIZE_OCTOQUAD)
        .build();
```

OctoQuad ticks-per-mm and TCP offsets remain millimeters. Pose and velocity become Pedro inches.

Encoder multipliers remain **inches per tick**. Prefer the upstream names `forwardTicksToInches` / `strafeTicksToInches` / `turnTicksToInches`, or the clearer aliases `forwardInchesPerTick`, `strafeInchesPerTick`, and `turnRadiansPerTick`.

## Centimeters, kilograms, and degrees

```java
Follower follower = new FollowerBuilder(new FollowerConstants().mass(10), hardwareMap)
        .setUnits(LengthUnit.CENTIMETERS, AngularUnit.DEGREES)
        .setStartingPose(60.96, 0, 90)
        .build();
Pose tile = follower.pose(60.96, 0, 0); // 24 inches internally
```

## Panels / tuners

Pedro poses are already inches. Panels Field drawing uses those inches directly. Standard tuner distances remain inches internally. Telemetry may show configured units through `follower.getPoseInConfiguredUnits()`.

## Migration from the prototype

The prototype `LengthUnit.use(...)` / `LengthUnit.active()` global API, `FollowerConstants.defaultsFor(...)`, `FollowerBuilder.lengthUnit(...)`, and `applyLengthUnit()` are gone. They changed Pedro’s internal unit system. This fork no longer does that.

```java
// prototype
public static final LengthUnit LENGTH = LengthUnit.use(LengthUnit.CENTIMETERS);
public static FollowerConstants followerConstants = new FollowerConstants().mass(10);
new FollowerBuilder(followerConstants, hardwareMap).lengthUnit(LENGTH);

// later prototype
FollowerConstants.defaultsFor(LENGTH);
new Pose(60.96, 0, 0); // was centimeters inside Pedro

// current
public static final PedroUnits UNITS = new PedroUnits(
        LengthUnit.CENTIMETERS, MassUnit.KILOGRAMS, AngularUnit.RADIANS);
public static FollowerConstants followerConstants = new FollowerConstants().mass(10);
Follower follower = new FollowerBuilder(followerConstants, hardwareMap)
        .setUnits(UNITS)
        .build();
Pose end = follower.pose(60.96, 0, 0); // centimeters in, inches stored
```

Existing inch-based TeamCode that never calls `.setUnits(...)` does not need pose or path number changes.

## Backward compatibility

* Existing `Pose` constructors and getters remain inches/radians.
* Existing mass setters remain kilograms.
* Existing path constraints remain inches/radians.
* Existing drivetrain constants remain inches per second.
* Existing encoder fields remain inches per tick.
* Hardware `DistanceUnit` settings still work and are not overwritten.
* No global unit initialization is required.
* Caller-owned constants objects are not rewritten unless you use configured builder setters, which copy first.

`FollowerBuilder.pathConstraints(...)` does not call `PathConstraints.setDefaultConstraints(...)`, so constructing one follower cannot change another’s defaults.

## Known limitations

* Encoder localizers still use process-wide static tick-to-distance fields (upstream pattern). Two encoder localizers in one process can overwrite those statics.
* Kalman filters initialize variance to `1` (upstream).
* Configured path-builder numeric overloads are not added; create canonical `Pose` objects with `follower.pose(...)` and use existing path APIs.

## Upstream synchronization

```bash
git fetch upstream
git merge upstream/main
```

The `upstream` remote points at `Pedro-Pathing/PedroPathing`. AGP is 8.13.2 and `compileSdk` is 34 here so this tree can be an `includeBuild` of an FTC SDK 11.2 project; upstream remains 8.7.3 / compileSdk 30.

## Build / composite build

Keep Maven coordinates `com.pedropathing:ftc` so this stays a substitute. From an Allsparks FTC project:

```gradle
includeBuild('../PedroPathing')
```

and `implementation 'com.pedropathing:ftc:2.2.0-SNAPSHOT'`. Composite build replaces the Maven artifact with this fork.

Java 8 / Android minSdk 21. Publishable modules: `:core` and `:ftc`.

## Test commands

```bash
./gradlew test
./gradlew :core:test :ftc:test
./gradlew :core:assemble :ftc:assemble
./gradlew dokkaGenerate
```

On Windows use `gradlew.bat`.
