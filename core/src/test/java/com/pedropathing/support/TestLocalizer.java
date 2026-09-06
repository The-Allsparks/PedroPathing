package com.pedropathing.support;

import com.pedropathing.geometry.Pose;
import com.pedropathing.localization.Localizer;
import com.pedropathing.math.Vector;

public class TestLocalizer implements Localizer {
    private Pose pose = new Pose();
    private Pose velocity = new Pose();

    @Override
    public Pose getPose() {
        return pose;
    }

    @Override
    public Pose getVelocity() {
        return velocity;
    }

    @Override
    public Vector getVelocityVector() {
        return velocity.getAsVector();
    }

    @Override
    public void setStartPose(Pose setStart) {
        pose = setStart;
    }

    @Override
    public void setPose(Pose setPose) {
        pose = setPose;
    }

    @Override
    public void update() {
    }

    @Override
    public double getTotalHeading() {
        return pose.getHeading();
    }

    @Override
    public double getForwardMultiplier() {
        return 1;
    }

    @Override
    public double getLateralMultiplier() {
        return 1;
    }

    @Override
    public double getTurningMultiplier() {
        return 1;
    }

    @Override
    public void resetIMU() {
    }

    @Override
    public double getIMUHeading() {
        return Double.NaN;
    }

    @Override
    public boolean isNAN() {
        return false;
    }
}
