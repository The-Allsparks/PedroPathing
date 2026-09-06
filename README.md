# Pedro Pathing (Allsparks fork)

Fork of [Pedro-Pathing/PedroPathing](https://github.com/Pedro-Pathing/PedroPathing) used by **[The Allsparks](https://github.com/The-Allsparks)** (FTC Team **36117**).

Upstream docs and tuning: [pedropathing.com](https://pedropathing.com/). Discord: [Pedro Pathing](https://discord.gg/2GfC4qBP5s).

> **Disclaimer:** This is a community fork. It is **not** affiliated with or endorsed by Pedro Pathing, FIRST, or other referenced vendors. Keep the BSD 3-Clause license. Merge upstream `main` regularly.

## Why this fork exists

Upstream Pedro treats poses, paths, and most tuners as **inches**. Pinpoint/OTOS can pick an FTC `DistanceUnit`, but drive-encoder constants, dashboard drawing, and Quickstart tuners still say inches.

This fork adds a selectable **length unit** so a team can run the whole follower in inches, centimeters, millimeters, or meters without converting at the TeamCode boundary.

Android Gradle Plugin is **8.13.2** (same as FTC SDK 11.2) so this tree can be an `includeBuild` of an FtcRobotController project. Upstream remains 8.7.3; expect a merge conflict there.

## Selecting a unit

Inches remain the default. To use centimeters:

```java
import com.pedropathing.math.LengthUnit;

public static FollowerConstants followerConstants =
        new FollowerConstants()
                .mass(10)
                .lengthUnit(LengthUnit.CENTIMETERS);
```

Then write poses, path distances, robot size, and encoder multipliers in that unit:

```java
Pose start = new Pose(0, 0, 0);
Pose end = new Pose(60.96, 0, 0); // one FTC tile, in centimeters
```

`FollowerBuilder` copies the follower unit onto Pinpoint `distanceUnit` and OTOS `linearUnit`. Encoder tick multipliers (`forwardTicksToInches` and the `forwardTicksToDistance` aliases) are “ticks to the selected unit,” not always inches.

Mass stays kilograms. Heading stays radians.

## Panels field overlay

Panels Field is still inch-based. TeamCode drawing should convert with `LengthUnit.toInches` before calling `panelsField.moveCursor`. The BumbleBee Tuning copy in `FtcRobotController` does this.

## Syncing upstream

```bash
git fetch upstream
git merge upstream/main
```

The `upstream` remote points at `Pedro-Pathing/PedroPathing`. Resolve conflicts in `LengthUnit`, `FollowerConstants.lengthUnit`, and hardware localizers first.

## Install

Keep using Maven coordinates `com.pedropathing:ftc` so this stays a drop-in substitute. From an Allsparks FTC project:

```gradle
includeBuild('../PedroPathing')
```

and the existing `implementation 'com.pedropathing:ftc:2.1.2'` line. Composite build replaces the Maven artifact with this fork.
