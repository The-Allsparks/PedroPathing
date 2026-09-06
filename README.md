# Pedro Pathing (Allsparks fork)

Fork of [Pedro-Pathing/PedroPathing](https://github.com/Pedro-Pathing/PedroPathing) used by **[The Allsparks](https://github.com/The-Allsparks)** (FTC Team **36117**).

Upstream docs and tuning: [pedropathing.com](https://pedropathing.com/). Discord: [Pedro Pathing](https://discord.gg/2GfC4qBP5s).

> **Disclaimer:** This is a community fork. It is **not** affiliated with or endorsed by Pedro Pathing, FIRST, or other referenced vendors. Keep the BSD 3-Clause license. Merge upstream `main` regularly.

## Why this fork exists

Upstream Pedro treats poses, paths, and most tuners as **inches**. Pinpoint/OTOS can pick an FTC `DistanceUnit`, but drive-encoder constants, dashboard drawing, and Quickstart tuners still say inches.

This fork adds a selectable **length unit** so a team can run the whole follower in inches, centimeters, millimeters, meters, or feet without converting at the TeamCode boundary. Inch-authored Pedro defaults live in `LengthAnchors` and are rescaled when you pick a unit.

Android Gradle Plugin is **8.13.2** (same as FTC SDK 11.2) so this tree can be an `includeBuild` of an FtcRobotController project. Upstream remains 8.7.3; expect a merge conflict there.

## Selecting a unit

Inches remain the default. Set the unit once on `LengthUnit`; constructors, tuners, and hardware adapters look up `LengthUnit.active()` so you do not pass a unit at every call site.

```java
import com.pedropathing.math.LengthUnit;

public static final LengthUnit LENGTH = LengthUnit.use(LengthUnit.CENTIMETERS);

public static FollowerConstants followerConstants =
        new FollowerConstants().mass(10);
```

Put `LENGTH` above the other static fields so `use(...)` runs first. `FollowerConstants`, `MecanumConstants`, `SwerveConstants`, and the short `PathConstraints` constructors then convert `LengthAnchors` inch defaults into that unit. `applyLengthUnit()` with no arguments does the same lookup, and is a no-op if the object is already in the active unit.

Then write poses, path distances, robot size, and encoder multipliers in that unit:

```java
Pose start = new Pose(0, 0, 0);
Pose end = new Pose(60.96, 0, 0); // one FTC tile, in centimeters
```

`LengthAnchors.of(48)` and `LengthUnit.ofInches(48)` also use the active unit. `FollowerBuilder` copies that unit onto Pinpoint `distanceUnit` and OTOS `linearUnit`. Encoder tick fields are `forwardTicksToDistance` / `strafeTicksToDistance` (ticks to the selected length unit). Turning uses `turnTicksToRadians`. Upstream `*TicksToInches` setters remain as deprecated wrappers.

Mass stays kilograms. Heading stays radians. Heading PIDF is not rescaled.

FTC `DistanceUnit` has no foot. `LengthUnit.FEET` maps Pinpoint/OTOS to inches; `PoseConverter` converts those hardware poses into feet. Specify Pinpoint/OTOS pod offsets in inches when the follower unit is feet.

## Panels field overlay

Panels Field is still inch-based. TeamCode drawing should convert with `LengthUnit.inInches` (or the instance `toInches`) before calling `panelsField.moveCursor`. The BumbleBee Tuning copy in `FtcRobotController` does this. Tuner pull/line/curve/velocity/radius distances start as the inch `LengthAnchors` values and are converted on first tuner select.

## Syncing upstream

```bash
git fetch upstream
git merge upstream/main
```

The `upstream` remote points at `Pedro-Pathing/PedroPathing`. Resolve conflicts in `LengthUnit`, `LengthAnchors`, `FollowerConstants.lengthUnit`, and hardware localizers first.

## Install

Keep using Maven coordinates `com.pedropathing:ftc` so this stays a drop-in substitute. From an Allsparks FTC project:

```gradle
includeBuild('../PedroPathing')
```

and the existing `implementation 'com.pedropathing:ftc:2.2.0-SNAPSHOT'` line. Composite build replaces the Maven artifact with this fork.
