# Allsparks Pedro 3 track

This branch **is** BumbleBee's live Pedro. `FtcRobotController` includeBuilds `com.pedropathing:revhub` + `:core` from this checkout.

Upstream docs: [pedropathing.com](https://pedropathing.com/). Official repo: [Pedro-Pathing/PedroPathing](https://github.com/Pedro-Pathing/PedroPathing).

`origin/main` is the abandoned 2.2 `:ftc` fork. Do not merge it into this branch or point includeBuild at `:ftc`.

## Allsparks deltas on this branch

1. **Encoder / heading / pose injection.** Every REV localizer can consume a cycle-level cache instead of reading hardware in `update()`. HardwareMap constructors stay. Injected reset is a software rebase (or a no-op on I2C pose devices); motor-backed reset still uses `STOP_AND_RESET_ENCODER`. Official Pedro closed encoder injection as unwanted in core ([PR 182](https://github.com/Pedro-Pathing/PedroPathing/pull/182)); Allsparks keeps it so PULSE can be the only hardware read in a loop.
   - Drive / two-wheel / three-wheel: `IntSupplier` ticks, plus heading `DoubleSupplier` when an IMU is in the math
   - Pinpoint / OTOS / OctoQuad: `MotionStateSource` pose+velocity snapshots. Injected `setPose` / `reset` do not write I2C
2. **Drive-encoder localizer.** Official 3.0 dropped it. BumbleBee still uses the four drive motors as odometry. `DriveEncoderConfig` + `DriveEncoderLocalizer` restore that math, with the same HardwareMap and injected-source constructors as the other REV localizers.
3. **AGP 8.13.2.** Official 3.0.1 ships AGP 8.7.3. FTC SDK 12's robot app uses 8.13.2; includeBuild forbids mixing them.
4. **UTF-8 Java compile.** Official FusionLocalizer comments use Unicode; Windows default encoding fails `:core:compileJava`.

## Not ported

- `FollowerBuilder` (TeamCode constructs `Follower(localizer, drivetrain, algorithm)`)
- `.setUnits(...)` / `PedroUnits` (TeamCode paths are inches)
- 2.2 `Tuning.java` menu (TeamCode ships a slim localization stick test)
- Pinpoint / OTOS TeamCode wiring if the shop later mounts those sensors

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

I2C pose devices (Pinpoint, OTOS, OctoQuad) inject a cached `MotionState`. TeamCode binds the sensor on `ReadBus.I2C`; Pedro must not call `pinpoint.update()` / `otos.getPosition()` / `octoQuad.readLocalizerData()` in the same loop.

```java
Localizer localizer = new PinpointLocalizer(
        () -> MotionState.ofVelocity(
                new Pose(pulse.pinpointX, pulse.pinpointY, pulse.pinpointHeading),
                new Velocity(pulse.pinpointVx, pulse.pinpointVy, pulse.pinpointOmega)));
```

OTOS snapshots that are still in sensor heading go through `OTOSLocalizer.fromSensor(...)` before injection.
