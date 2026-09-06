# Pedro Pathing (Allsparks fork)

Fork of [Pedro-Pathing/PedroPathing](https://github.com/Pedro-Pathing/PedroPathing) used by **[The Allsparks](https://github.com/The-Allsparks)** (FTC Team **36117**).

Upstream docs and tuning: [pedropathing.com](https://pedropathing.com/). Discord: [Pedro Pathing](https://discord.gg/2GfC4qBP5s).

> **Disclaimer:** This is a community fork. It is **not** affiliated with or endorsed by Pedro Pathing, FIRST, or other referenced vendors. Keep the BSD 3-Clause license. Merge upstream `main` regularly.

## Why this fork exists

Upstream Pedro treats poses, paths, and most tuners as **inches**. Pinpoint/OTOS can pick an FTC `DistanceUnit`, but drive-encoder constants, dashboard drawing, and Quickstart tuners still say inches.

This fork adds a selectable **follower length unit** so a team can run the whole follower in inches, centimeters, meters, or feet. Inch-authored Pedro defaults live in `LengthAnchors` and are generated for the selected configuration. Millimeters are **not** a selectable follower unit; they remain an internal hardware-boundary helper for devices such as OctoQuad.

## Supported follower units

Exactly:

* `LengthUnit.INCHES`
* `LengthUnit.CENTIMETERS`
* `LengthUnit.METERS`
* `LengthUnit.FEET`

Millimeters may appear only as hardware-native measurements (`DistanceUnit.MM`, OctoQuad `*_MM` fields, `LengthUnit.toMillimeters` / `fromMillimeters`). They are not a `LengthUnit` enum constant.

## Basic setup

The selected unit belongs to the follower configuration. It is immutable after construction. Creating one follower cannot change another.

```java
import com.pedropathing.math.LengthUnit;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;

public static final LengthUnit LENGTH = LengthUnit.CENTIMETERS;

public static FollowerConstants followerConstants =
        FollowerConstants.defaultsFor(LENGTH).mass(10);

public static Follower createFollower(HardwareMap hardwareMap) {
    return new FollowerBuilder(followerConstants, hardwareMap)
            .lengthUnit(LENGTH)
            // localizer and drivetrain configuration
            .build();
}
```

Call `.lengthUnit(...)` before configuring the localizer or drivetrain. If omitted, the builder uses `followerConstants.getLengthUnit()`.

Then write poses in that unit. Do not pass a unit at every path or pose site:

```java
Pose start = new Pose(0, 0, 0);
Pose end = new Pose(60.96, 0, 0); // one FTC tile, in centimeters
Pose twoFeet = new Pose(2, 0, 0); // when LENGTH is FEET
```

Mass stays kilograms. Heading stays radians. Times stay in their existing units. Dimensionless coefficients are not rescaled.

## Localizer examples

Hardware devices keep their own unit. Conversion happens once inside the localizer.

**Pinpoint, follower centimeters, Pinpoint inches**

```java
new FollowerBuilder(FollowerConstants.defaultsFor(LengthUnit.CENTIMETERS), hardwareMap)
        .lengthUnit(LengthUnit.CENTIMETERS)
        .pinpointLocalizer(new PinpointConstants().distanceUnit(DistanceUnit.INCH))
        .build();
```

**Pinpoint, follower feet, Pinpoint inches**

```java
new FollowerBuilder(FollowerConstants.defaultsFor(LengthUnit.FEET), hardwareMap)
        .lengthUnit(LengthUnit.FEET)
        .pinpointLocalizer(new PinpointConstants().distanceUnit(DistanceUnit.INCH))
        .build();
```

Pinpoint position, velocity, `setPose()`, and `setX`/`setY` convert between inches and feet. Pod offsets and custom encoder resolution stay in Pinpoint's `distanceUnit`.

**OTOS, follower feet, OTOS inches**

```java
new FollowerBuilder(FollowerConstants.defaultsFor(LengthUnit.FEET), hardwareMap)
        .lengthUnit(LengthUnit.FEET)
        .OTOSLocalizer(new OTOSConstants().linearUnit(DistanceUnit.INCH))
        .build();
```

OTOS position, velocity, and `setPose()` convert. The OTOS sensor offset stays in the OTOS linear unit.

**OctoQuad, follower feet, hardware millimeters**

```java
new FollowerBuilder(FollowerConstants.defaultsFor(LengthUnit.FEET), hardwareMap)
        .lengthUnit(LengthUnit.FEET)
        .octoQuadLocalizer(octoQuadConstants, OctoQuadLocalizer.InitMode.INITIALIZE_OCTOQUAD)
        .build();
```

OctoQuad ticks-per-mm and TCP offsets remain millimeters. Pose and velocity are converted to feet.

**Encoder localizer, centimeters per tick**

```java
new DriveEncoderConstants()
        .forwardTicksToDistance(0.05) // cm / tick
        .strafeTicksToDistance(0.05)
        .turnTicksToRadians(0.001)
        .robotWidth(30)
        .robotLength(35);
```

Encoder multipliers are **selected follower unit per tick**. Robot width, length, and deadwheel pod offsets for encoder localizers are in the follower unit.

## Feet behavior

FTC `DistanceUnit` has no foot. This fork does **not** treat `DistanceUnit.INCH` as feet.

A hardware adapter may use inches, centimeters, meters, or millimeters internally. It converts hardware results to feet before constructing Pedro poses, and converts follower feet back to the configured hardware unit for `setPosition` / `setPose` / dimensional hardware configuration.

## Hardware unit independence

`FollowerBuilder` does not overwrite Pinpoint `distanceUnit` or OTOS `linearUnit`. Caller-owned constants objects are not mutated to satisfy follower units. The builder copies drivetrain and follower constants when converting.

## Path constraint semantics

* `new PathConstraints(0.99, 100, 1, 1)` stores inch-authored Pedro velocity/translational defaults and is labeled inches.
* `PathConstraints.defaultsFor(unit)` converts those inch defaults exactly once.
* `PathConstraints.inUnit(unit, ...)` stores caller values already expressed in `unit` and will not convert them again.
* `PathConstraints.defaultConstraints` remains the inch-authored upstream default. Unit-aware code does not mutate it.
* Attaching constraints to a follower converts a **copy** using the stored source unit. Incompatible units are not guessed; the stored unit is the source.

## Panels / Quickstart

Panels Field remains inch-based. Convert follower poses and path points to inches only at the drawing boundary with `LengthDrawing.toInches(...)`. Do not pass converted poses back into follower or path logic.

The Allsparks `FtcRobotController` TeamCode copy of Tuning uses `follower.getLengthUnit()` and `LengthDrawing`. Upstream Pedro Quickstart still hard-codes inches until those files are updated:

* `TeamCode/.../pedroPathing/Constants.java` — `defaultsFor` + `FollowerBuilder.lengthUnit`
* `TeamCode/.../pedroPathing/Tuning.java` — tuner distances via `LengthAnchors.of(inches, unit)` and Panels conversion via `LengthDrawing`

This repository includes `SelectableUnitSetupExample` as the in-tree integration fixture. The library change alone does not make an unmodified upstream Quickstart unit-aware.

## Backward compatibility

* `new FollowerConstants()`, `new MecanumConstants()`, and `new PathConstraints(t, timeout, ...)` remain inch-based.
* Inch-mode numeric defaults match upstream Pedro.
* Deprecated encoder setters `forwardTicksToInches`, `strafeTicksToInches`, and `turnTicksToInches` still write the new fields. `turnTicksToInches` was always a heading scale; it is a historical alias for `turnTicksToRadians`.
* Direct public field assignment to the old `*TicksToInches` **field names** is a breaking change. Use the new field names or the deprecated setters.
* The prototype `LengthUnit.use(...)` / `LengthUnit.active()` global API has been removed. It was process-wide mutable state and is not safe for two followers.

### Migration from the prototype global API

```java
// prototype
public static final LengthUnit LENGTH = LengthUnit.use(LengthUnit.CENTIMETERS);
public static FollowerConstants followerConstants = new FollowerConstants().mass(10);

// current
public static final LengthUnit LENGTH = LengthUnit.CENTIMETERS;
public static FollowerConstants followerConstants =
        FollowerConstants.defaultsFor(LENGTH).mass(10);
```

### Migration from standard inch-based upstream Pedro

No pose or path number changes are required if you stay in inches. To switch units, generate defaults with `defaultsFor(unit)`, pass `.lengthUnit(unit)` on the builder, and rewrite poses in that unit.

## Known limitations

* Encoder localizers still use process-wide static tick-to-distance fields (upstream pattern). Two encoder localizers in one process can overwrite those statics.
* Kalman filters initialize variance to `1` (upstream). Covariance scaling keeps steady-state behavior equivalent; the first update still starts from that constant.
* Direct assignment to removed `*TicksToInches` fields will not compile.
* `Pose.mirror()` with no arguments still uses the upstream 141.5 inch number. Non-inch followers should call `pose.mirror(unit)` or `pose.mirror(unit.mirrorFieldLength())`.

## Upstream synchronization

```bash
git fetch upstream
git merge upstream/main
```

The `upstream` remote points at `Pedro-Pathing/PedroPathing`. Expect conflicts in `LengthUnit`, `LengthContext`, `FollowerConstants`, `PathConstraints`, `FollowerBuilder`, hardware localizers, and `PoseConverter`. AGP is 8.13.2 and `compileSdk` is 34 here so this tree can be an `includeBuild` of an FTC SDK 11.2 project; upstream remains 8.7.3 / compileSdk 30.

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
