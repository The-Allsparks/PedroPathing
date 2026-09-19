package com.pedropathing.revhub.localizers;

import com.pedropathing.config.ConfigVar;
import com.pedropathing.config.Configuration;

/**
 * Drive-motor odometry. Official Pedro 3 dropped this localizer; Allsparks keeps it because
 * BumbleBee currently uses the four drive encoders before Pinpoint or dead wheels.
 */
public class DriveEncoderConfig {
    public final ConfigVar<String> frontLeftName = ConfigVar.required();
    public final ConfigVar<String> frontRightName = ConfigVar.required();
    public final ConfigVar<String> backLeftName = ConfigVar.required();
    public final ConfigVar<String> backRightName = ConfigVar.required();

    public final ConfigVar<Double> robotWidth = ConfigVar.required();
    public final ConfigVar<Double> robotLength = ConfigVar.required();

    public final ConfigVar<Double> forwardTicksToInches = ConfigVar.required();
    public final ConfigVar<Double> strafeTicksToInches = ConfigVar.required();
    public final ConfigVar<Double> turnTicksToRadians = ConfigVar.required();

    public final ConfigVar<Double> frontLeftDirection = ConfigVar.required();
    public final ConfigVar<Double> frontRightDirection = ConfigVar.required();
    public final ConfigVar<Double> backLeftDirection = ConfigVar.required();
    public final ConfigVar<Double> backRightDirection = ConfigVar.required();

    public DriveEncoderConfig(Configuration<DriveEncoderConfig> config) {
        config.configure(this);
    }
}
