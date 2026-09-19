# Allsparks Pedro 3 track

This branch is **not** the robot's live Pedro.

BumbleBee still includeBuilds the Allsparks **2.2** fork (`:ftc` + `:core`) from `origin/main`. This `pedro3` branch is official Pedro **3.0.1** plus the Allsparks pieces TeamCode still needs.

Upstream docs: [pedropathing.com](https://pedropathing.com/). Official repo: [Pedro-Pathing/PedroPathing](https://github.com/Pedro-Pathing/PedroPathing).

## Why a parallel branch

Official 3.0 renamed the FTC module (`ftc` → `revhub`), replaced `FollowerBuilder` with `Follower(localizer, drivetrain, algorithm)`, and dropped drive-encoder localization. Merging that onto Allsparks `main` would break BumbleBee's `includeBuild` the same day.

Keep two tracks until TeamCode is rewritten:

| Track | Branch | Module | Robot |
| ----- | ------ | ------ | ----- |
| Live 2.2 | `origin/main` | `com.pedropathing:ftc` | BumbleBee includeBuild |
| Pedro 3 | `origin/pedro3` | `com.pedropathing:revhub` | Not wired yet |

Do not switch `FtcRobotController/settings.gradle` until the migration checklist in [MIGRATION.md](MIGRATION.md) is done.

## Allsparks deltas on this branch

1. **Encoder / heading injection.** REV localizers can take `IntSupplier` / heading callbacks. HardwareMap constructors stay. Injected reset is a software rebase; motor-backed reset still uses `STOP_AND_RESET_ENCODER`. Official Pedro closed this as unwanted in core ([PR 182](https://github.com/Pedro-Pathing/PedroPathing/pull/182)); Allsparks keeps it so PULSE can be the only `getCurrentPosition()` in a loop.
2. **Drive-encoder localizer.** Official 3.0 dropped it. BumbleBee still uses the four drive motors as odometry. `DriveEncoderConfig` + `DriveEncoderLocalizer` restore that math, with the same HardwareMap and injected-source constructors as the other REV localizers.

## Not ported yet

- `FollowerBuilder`
- `.setUnits(...)` / `PedroUnits` (TeamCode still speaks centimeters on 2.2)
- 2.2 `Tuning.java` / `FollowerConstants` shape
- Pinpoint / OTOS adapters if TeamCode later needs them on 3.x

## PULSE

Inject raw hardware-sign ticks. Pedro applies `DriveEncoderConfig` direction multipliers.

```java
Localizer localizer = new DriveEncoderLocalizer(
        config,
        pulse.frontLeftTicks,
        pulse.frontRightTicks,
        pulse.backLeftTicks,
        pulse.backRightTicks);
Follower follower = new Follower(localizer, drivetrain, algorithm);
```
