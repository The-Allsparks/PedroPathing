# BumbleBee Pedro 2.2 → 3.0 migration

Robot stays on Allsparks Pedro **2.2** until this checklist is finished. `pedro3` is a parallel fork track from official 3.0.1.

## Do not do yet

- Merge official 3.x into Allsparks `PedroPathing` `main`
- Change `FtcRobotController/settings.gradle` includeBuild substitutions
- Change `build.dependencies.gradle` from `com.pedropathing:ftc:2.2.0-SNAPSHOT`
- Rewrite `Constants.java` / `Tuning.java` on `bumblebee` as the first PR

## API gaps TeamCode must absorb

| 2.2 (live) | 3.0 (`pedro3`) |
| ---------- | -------------- |
| `com.pedropathing:ftc` | `com.pedropathing:revhub` |
| `FollowerBuilder` | `new Follower(localizer, drivetrain, algorithm)` |
| `.mecanumDrivetrain(MecanumConstants)` | `new Mecanum(hardwareMap, MecanumConfig)` |
| `.driveEncoderLocalizer(...)` | `new DriveEncoderLocalizer(...)` (Allsparks-restored) |
| `.setUnits(PedroUnits centimeters)` | Not ported. Paths are inches until a later units layer |
| `FollowerConstants` + PIDF tuners | `Foresight` + `ForesightConfig` (required controllers, max velocity, natural deceleration) |
| `follower.getPose()` | `localizer.pose()` / `follower` debug log |
| PULSE `IntSupplier` overload on `FollowerBuilder` | `DriveEncoderLocalizer(config, fl, fr, bl, br)` |

## includeBuild switch (later)

When TeamCode compiles against this branch, point the existing `../PedroPathing` checkout at `pedro3` **or** includeBuild a second worktree, then:

```gradle
includeBuild('../PedroPathing') {
    dependencySubstitution {
        substitute module('com.pedropathing:revhub') using project(':revhub')
        substitute module('com.pedropathing:core') using project(':core')
    }
}
```

`implementation 'com.pedropathing:ftc:...'` becomes `implementation 'com.pedropathing:revhub:3.0.1'`.

TeamCode still needs an explicit `:core` jar on the APK (AGP does not dex the java-library transitive). Keep the `fileTree` + `preBuild` `includedBuild('PedroPathing').task(':core:jar')` pattern, pointed at this tree.

## TeamCode rewrite (later)

1. Replace `Constants.createFollower` with Mecanum + DriveEncoderLocalizer + Foresight construction.
2. Keep a PULSE injection overload that does **not** reread motors.
3. Vendor a new Tuning menu from the Pedro 3 Quickstart. Do not try to compile 2.2 `Tuning.java` against 3.0.
4. Decide centimeters: port `setUnits` onto 3.0, or retune paths in inches.
5. Floor-test Motor Test + Drive first; Pedro Line stays `@Disabled` until localization tuners pass.

## Validation

- `./gradlew :revhub:test` on this branch (injection + drive-encoder tests)
- After includeBuild switch: TeamCode compile, Hub deploy, PULSE Pedro Line still one encoder read per loop
