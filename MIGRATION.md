# BumbleBee Pedro 2.2 → 3.0

BumbleBee includeBuilds this `pedro3` branch. The 2.2 `:ftc` fork on `origin/main` is abandoned.

## Live includeBuild

```gradle
includeBuild('../PedroPathing') {
    dependencySubstitution {
        substitute module('com.pedropathing:revhub') using project(':revhub')
        substitute module('com.pedropathing:core') using project(':core')
    }
}
```

`implementation 'com.pedropathing:revhub:3.0.1'`

TeamCode still needs an explicit `:core` jar on the APK (AGP does not dex the java-library transitive). Keep the `fileTree` + `preBuild` `includedBuild('PedroPathing').task(':core:jar')` pattern.

## TeamCode

- `Constants.createFollower` builds Mecanum + DriveEncoderLocalizer + Foresight
- PULSE injection overload does not reread motors
- Pinpoint / OTOS / OctoQuad HardwareMap constructors stay; injected `MotionStateSource` constructors do not reread I2C
- `Tuning.java` is a slim localization stick test, not the 2.2 Quickstart menu
- Path poses are inches (`Lengths.TILE_INCHES`)

## Still later

- Vendor official Pedro 3 autotune Procedures when `com.pedropathing.tuning` is in the includeBuild
- Replace Foresight placeholders from floor tuners
- Pinpoint / OTOS if the shop adds those sensors
