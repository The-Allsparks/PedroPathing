# Pedro Pathing (Allsparks fork)

Fork of [Pedro-Pathing/PedroPathing](https://github.com/Pedro-Pathing/PedroPathing) used by **[The Allsparks](https://github.com/The-Allsparks)** (FTC Team **36117**).

Upstream docs and tuning: [pedropathing.com](https://pedropathing.com/). Discord: [Pedro Pathing](https://discord.gg/2GfC4qBP5s).

> **Disclaimer:** This is a community fork. It is **not** affiliated with or endorsed by Pedro Pathing, FIRST, or other referenced vendors. Keep the BSD 3-Clause license. Merge upstream `main` regularly.

## Why this fork exists

Upstream Pedro calculates in **inches**, **kilograms**, and **radians**. This fork adds a TeamCode-facing unit conversion layer so a team can enter and display values in their preferred units without changing Pedro’s internal math.

`.setUnits(...)` selects those units once per `FollowerBuilder`. After that, TeamCode pose, path, heading, and telemetry interaction uses the selected units. Pedro still calculates in inches, kilograms, and radians behind that boundary.

## Canonical internal units

Pedro always calculates in:

* Distance: inches
* Velocity: inches per second
* Acceleration: inches per second squared
* Mass: kilograms
* Angles: radians
* Angular velocity: radians per second
* Existing timeout/time units unchanged

`.setUnits(...)` does **not** rescale PID coefficients, covariance, or drivetrain defaults. It converts TeamCode-facing poses, paths, headings, and configured builder setters at the follower boundary.

## Supported interface units

Length: `INCHES`, `FEET`, `CENTIMETERS`, `METERS`

Mass: `KILOGRAMS`, `POUNDS`

Angles: `RADIANS`, `DEGREES` (`AngularUnit` in core, to avoid colliding with the FTC SDK `AngleUnit`)

Millimeters are **not** a selectable interface unit. They may be used internally by hardware adapters such as OctoQuad.

## One TeamCode unit system

If you never call `.setUnits(...)`, behavior matches upstream Pedro (inches, kilograms, and radians).

If you do call `.setUnits(...)`, do not mix. Poses, path control points, heading interpolation, `getPose()`, `atPose()`, `turnTo()`, mass, drivetrain velocity, encoder geometry, ticks-to-distance, and telemetry all use those units:

```java
Follower follower = new FollowerBuilder(constants, hardwareMap)
        .setUnits(LengthUnit.FEET, MassUnit.POUNDS, AngularUnit.DEGREES)
        .setMass(23.5)
        .setStartingPose(2, 4, 90)
        .pinpointLocalizer(pinpointConstants)
        .mecanumDrivetrain(mecanumConstants)
        .build();

Pose start = follower.pose(2, 4, 90);
Pose end = follower.pose(5, 3, 180);
follower.pathBuilder()
        .addPath(new BezierLine(start, end))
        .setConstantHeadingInterpolation(180)
        .build();

Pose now = follower.getPose();
double xFeet = now.getX();
double headingDeg = now.getHeading();
double massLb = follower.getMass();
```

Internally that starting pose is 24 inches, 48 inches, and π/2 radians. You do not read or write those canonical numbers from TeamCode after `.setUnits(...)`.

`new Pose(24, 48, Math.PI / 2)` has no unit context. After `.setUnits(FEET, DEGREES)`, those numbers would mean 24 feet and π/2 degrees. Create poses with `follower.pose(...)` or `units.pose(...)`.

Constants objects passed into `FollowerBuilder` after `.setUnits(...)` are also in the selected units. Unchanged Pedro library defaults stay as Pedro's inch/kg/radian defaults so Strafer numbers are not reinterpreted as feet or pounds:

```java
new FollowerConstants().mass(23.5);          // pounds if setUnits selected pounds
mecanumConstants.xVelocity(6.78);            // feet/s if setUnits selected feet
localizerConstants.robotWidth(1.2);          // feet
localizerConstants.forwardTicksToInches(0.02); // feet per tick
```

Pinpoint `distanceUnit`, OTOS `linearUnit`, and OctoQuad millimeters stay hardware-device units.

`ConfiguredPose` is optional telemetry with unit symbols. `follower.getPose()` is already in the selected units.

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

## Poses, paths, and telemetry

```java
Pose start = follower.pose(2, 4, 90);
Pose target = follower.getUnits().pose(5, 3, 180);
follower.setStartingPose(start);
follower.pathBuilder()
        .addPath(new BezierLine(start, target))
        .setLinearHeadingInterpolation(90, 180)
        .build();

double x = follower.getPose().getX();
double heading = follower.getPose().getHeading();
ConfiguredPose labeled = follower.getPoseInConfiguredUnits();
```

Panels Field drawing still uses Pedro inches. Use `follower.getInternalPose()` there.

## Path constraints

`PathConstraints` objects store inches per second, inches, and radians. Build them from configured units, or set completion tolerances on `FollowerBuilder` / `PathBuilder`:

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

Encoder field names stay `forwardTicksToInches` / `strafeTicksToInches` / `turnTicksToInches`. After `.setUnits(...)`, those numbers are **configured length per tick** (and configured angle per tick for turn). Internally they become inches per tick / radians per tick.

## Centimeters, kilograms, and degrees

```java
Follower follower = new FollowerBuilder(new FollowerConstants().mass(10), hardwareMap)
        .setUnits(LengthUnit.CENTIMETERS, AngularUnit.DEGREES)
        .setStartingPose(60.96, 0, 90)
        .build();
Pose tile = follower.pose(60.96, 0, 0); // 60.96 cm in getX(); 24 inches internally
```

## Panels / tuners

Panels Field drawing uses Pedro inches. Draw with `follower.getInternalPose()`. Tuner telemetry and pull distances follow the units selected by `.setUnits(...)`.

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
Pose end = follower.pose(60.96, 0, 0); // centimeters, including getX()
```

Existing inch-based TeamCode that never calls `.setUnits(...)` does not need pose or path number changes.

## Backward compatibility

* If you never call `.setUnits(...)`, existing `Pose` constructors and getters remain inches/radians.
* If you never call `.setUnits(...)`, mass setters remain kilograms and drivetrain/encoder lengths remain inches.
* After `.setUnits(...)`, `FollowerBuilder` interprets caller-supplied mass, accelerations, drivetrain velocities, encoder geometry, and ticks-to-distance in the selected units. Caller-owned objects are copied before conversion.
* Unchanged Pedro library defaults are left in Pedro's canonical units.
* Hardware `DistanceUnit` settings still work and are not overwritten.
* No global unit initialization is required.

`FollowerBuilder.pathConstraints(...)` does not call `PathConstraints.setDefaultConstraints(...)`, so constructing one follower cannot change another’s defaults.

## Known limitations

* Encoder localizers still use process-wide static tick-to-distance fields (upstream pattern). Two encoder localizers in one process can overwrite those statics.
* Kalman filters initialize variance to `1` (upstream).
* `HeadingInterpolator.facingPoint(x, y)` and custom interpolators that read path geometry still use canonical path coordinates. Prefer `PathBuilder.setFacingPointHeadingInterpolation(x, y)` after `.setUnits(...)`.
* Pinpoint offsets follow Pinpoint `distanceUnit`. OTOS offsets follow OTOS `linearUnit`. OctoQuad ticks-per-mm stay millimeters.

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
