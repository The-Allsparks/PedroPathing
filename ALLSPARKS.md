# Allsparks Pedro 3 track

This branch **is** BumbleBee's live Pedro. `FtcRobotController` includeBuilds `com.pedropathing:revhub` + `:core` from this checkout.

Upstream docs: [pedropathing.com](https://pedropathing.com/). Official repo: [Pedro-Pathing/PedroPathing](https://github.com/Pedro-Pathing/PedroPathing).

`origin/main` is the abandoned 2.2 `:ftc` fork. Do not merge it into this branch or point includeBuild at `:ftc`.

## Allsparks deltas on this branch

1. **Encoder / heading injection.** REV localizers can take `IntSupplier` / heading callbacks. HardwareMap constructors stay. Injected reset is a software rebase; motor-backed reset still uses `STOP_AND_RESET_ENCODER`. Official Pedro closed this as unwanted in core ([PR 182](https://github.com/Pedro-Pathing/PedroPathing/pull/182)); Allsparks keeps it so PULSE can be the only `getCurrentPosition()` in a loop.
2. **Drive-encoder localizer.** Official 3.0 dropped it. BumbleBee still uses the four drive motors as odometry. `DriveEncoderConfig` + `DriveEncoderLocalizer` restore that math, with the same HardwareMap and injected-source constructors as the other REV localizers.
3. **AGP 8.13.2.** Official 3.0.1 ships AGP 8.7.3. FTC SDK 12's robot app uses 8.13.2; includeBuild forbids mixing them.
4. **UTF-8 Java compile.** Official FusionLocalizer comments use Unicode; Windows default encoding fails `:core:compileJava`.

## Not ported

- `FollowerBuilder` (TeamCode constructs `Follower(localizer, drivetrain, algorithm)`)
- `.setUnits(...)` / `PedroUnits` (TeamCode paths are inches)
- 2.2 `Tuning.java` menu (TeamCode ships a slim localization stick test)
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
